package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.*;
import com.mudosa.musinsa.order.application.dto.request.OrderCreateRequest;
import com.mudosa.musinsa.order.application.dto.response.OrderCreateResponse;
import com.mudosa.musinsa.order.application.dto.response.OrderDetailResponse;
import com.mudosa.musinsa.order.application.dto.response.OrderListResponse;
import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.CartItemRepository;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final UserRepository userRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public OrderCreateResponse createPendingOrder(OrderCreateRequest request, Long userId) {

        //ProductOption 매핑 & 주문 상품 유효성 확인
        Map<ProductOption, Integer> optionsWithQuantity = getProductOptionIntegerMap(request);

        //재고 확인
        validateStock(optionsWithQuantity);

        //주문 생성
        Order order = Order.create(
                userId,
                request.getCouponId(),
                optionsWithQuantity
        );

        Order savedOrder = orderRepository.save(order);

        return OrderCreateResponse.of(savedOrder.getId(), savedOrder.getOrderNo());
    }

    public PendingOrderResponse fetchPendingOrder(String orderNo) {
        // 주문 조회
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 사용자 정보 조회
        Long userId = order.getUserId();
        UserInfoDto userInfo = userRepository.findDtoById(userId);

        // 상품 목록 조회
        List<PendingOrderItem> orderProductsInfo = orderRepository.findOrderItems(orderNo);

        // 쿠폰 목록 조회
        List<OrderMemberCoupon> memberCoupons = memberCouponRepository.findOrderMemberCouponsByUserId(userId);

        return new PendingOrderResponse(
                orderNo,
                order.getTotalPrice().getAmount(),
                order.getTotalDiscount().getAmount(),
                orderProductsInfo,
                memberCoupons,
                userInfo.userName(),
                userInfo.currentAddress(),
                userInfo.contactNumber()
        );
    }

    public OrderDetailResponse fetchOrderDetail(String orderNo) {
        //주문 조회

        //사용자 정보 조회

        //상품 목록 조회

        //결제 정보 조회
        return null;
    }

    public OrderListResponse fetchOrderList(Long userId, Pageable pageable) {
        return null;
    }

    public void cancelOrder(String orderNo) {

    }

    @Transactional(readOnly = false)
    public Long completeOrder(String orderNo) {
        //주문 조회
        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Map<Long, Integer> quantityMap = order.getOrderProducts().stream()
                .collect(Collectors.toMap(
                        OrderProduct::getProductOptionId,
                        OrderProduct::getProductQuantity
                ));

        //재고 차감
        List<Long> optionIds = new ArrayList<>(quantityMap.keySet());

        List<ProductOption> productOptions = productOptionRepository.findByProductOptionIdInWithPessimisticLock(optionIds);

        productOptions.forEach(po->{
            Integer quantityToDeduct = quantityMap.get(po.getProductOptionId());
            po.deductStock(quantityToDeduct);
        });

        //주문 상태 변경
        order.complete();
        orderRepository.save(order);

        return order.getId();
    }

    public void deleteCartItems(Long orderId, Long userId) {
        //주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        //장바구니 삭제
        List<Long> productOptionIds = order.getOrderProducts().stream()
                .map(op -> op.getProductOption().getProductOptionId())
                .toList();

        cartItemRepository.deleteByUserIdAndProductOptionIdIn(
                userId,
                productOptionIds
        );
    }

    @Transactional(readOnly = false)
    public void rollbackOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            ProductOption productOption = productOptionRepository.findById(
                    orderProduct.getProductOption().getProductOptionId()
            ).orElseThrow();

            productOption.restoreStock(orderProduct.getProductQuantity());
        }

        order.rollbackStatus();
        orderRepository.save(order);
    }


    private void validateStock(Map<ProductOption, Integer> optionsWithQuantity) {
        //재고 확인
        List<InsufficientStockItem> insufficientItems = optionsWithQuantity.entrySet().stream()
                .filter(entry -> !entry.getKey().hasEnoughStock(entry.getValue()))
                .map(entry -> new InsufficientStockItem(
                        entry.getKey().getProductOptionId(),
                        entry.getValue(),
                        entry.getKey().getStockQuantity()
                ))
                .toList();

        if (!insufficientItems.isEmpty()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, insufficientItems);
        }
    }

    private Map<ProductOption, Integer> getProductOptionIntegerMap(OrderCreateRequest request) {
        List<Long> optionIds = request.getItems().stream()
                .map(OrderCreateItem::getProductOptionId)
                .toList();

        //상품 옵션 조회
        List<ProductOption> productOptions = productOptionRepository.findAllById(optionIds);

        //상품 옵션 Id 유효성 확인
        if(productOptions.size() != optionIds.size()){
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }

        List<Long> list = productOptions.stream().filter(po -> !po.getProduct().getIsAvailable()).map(ProductOption::getProductOptionId).toList();

        //주문 상품 유효성 확인
        if(!list.isEmpty()){
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_ORDER, list);
        }

        Map<Long, Integer> quantityMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderCreateItem::getProductOptionId,
                        OrderCreateItem::getQuantity
                ));

        return productOptions.stream()
                .collect(Collectors.toMap(
                        option -> option,
                        option -> quantityMap.get(option.getProductOptionId())
                ));
    }
}
