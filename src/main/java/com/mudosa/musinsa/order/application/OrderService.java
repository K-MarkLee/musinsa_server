package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import com.mudosa.musinsa.coupon.domain.service.MemberCouponService;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.*;
import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.order.domain.model.StockValidationResult;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.payment.application.dto.OrderValidationResult;
import com.mudosa.musinsa.product.application.CartService;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MemberCouponService memberCouponService;
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

    @Transactional(readOnly = true)
    public PendingOrderResponse fetchPendingOrder(String orderNo) {
        log.info("[Order] 주문서 조회 시작 - orderNo: {}", orderNo);

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

    @Transactional(readOnly = true)
    public OrderDetailResponse fetchOrderDetail(String orderNo) {
        //주문 조회

        //사용자 정보 조회

        //상품 목록 조회

        //결제 정보 조회
        return null;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeOrder(Long orderId) {
        log.info("주문 완료 처리 시작 - orderId: {}", orderId);

        /* 주문 조회 및 검증 */
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        /* 주문 상태 재검증 -> 동시성 제어를 위해 */
        order.validatePending();

        /* 주문 상품 재검증 */
        order.validateOrderProducts();

        log.info("주문 검증 완료 - orderId: {}, orderProducts: {}",
                orderId, order.getOrderProducts().size());

        /* 재고 차감 */
        order.decreaseStock();
        log.info("재고 차감 완료 - orderId: {}", orderId);

        /* 주문 상태 변경 */
        order.complete();
        orderRepository.save(order);

        log.info("주문 상태 변경 완료 - orderId: {}, status: COMPLETED", orderId);

        /* 주문 제품에 대한 장바구니 제품 삭제 */
        deleteCartItems(order);

        /* 쿠폰을 사용한 주문이라면 MemberCoupon 사용 처리 */
        useCouponIfExists(order);

        log.info("주문 완료 처리 성공 - orderId: {}", orderId);
    }

    private void deleteCartItems(Order order) {
        List<Long> productOptionIds = order.getOrderProducts().stream()
                .map(op -> op.getProductOption().getProductOptionId())
                .toList();

        cartService.deleteCartItemsByProductOptions(order.getUserId(), productOptionIds);

        log.info("장바구니 삭제 완료 - orderId: {}, count: {}",
                order.getId(), productOptionIds.size());
    }

    private void useCouponIfExists(Order order) {
        if (!order.hasCoupon()) {
            return;
        }

        try {
            memberCouponService.useMemberCoupon(
                    order.getUserId(),
                    order.getCouponId(),
                    order.getId()
            );

            log.info("쿠폰 사용 처리 완료 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId());

        } catch (Exception e) {
            log.error("쿠폰 사용 처리 실패 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackOrder(Long orderId) {
        log.warn("주문 롤백 시작 - orderId: {}", orderId);

        /* 재고 복구 */
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.restoreStock();
        log.info("재고 복구 완료 - orderId: {}", orderId);

        /* 주문 상태 복구 */
        order.rollback();
        orderRepository.save(order);

        /* 쿠폰 복구 */
        rollbackCouponIfUsed(order);

        log.warn("주문 롤백 완료 - orderId: {}, status: PENDING", orderId);
    }

    public boolean isOrderCompleted(Long orderId) {
        return orderRepository.findById(orderId).map(order -> order.getStatus().isCompleted()).orElse(false);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderValidationResult validateAndPrepareOrder(
            String orderNo,
            Long userId,
            BigDecimal requestAmount,
            Long couponId) {

        log.info("주문 검증 시작 - orderNo: {}, userId: {}, requestAmount: {}",
                orderNo, userId, requestAmount);

        // 1. 주문 조회
        Order order = orderRepository.findByOrderNoWithOrderProducts(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderNo));

        // 2. 사용자 권한 검증
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER,
                    "본인의 주문만 결제할 수 있습니다");
        }

        // 3. 주문 상태 검증
        order.validatePending();

        // 4. 재고 검증
        StockValidationResult stockValidationResult = order.validateStock();
        if (stockValidationResult.hasInsufficientStock()) {
            log.warn("재고 부족 - orderNo: {}", orderNo);
            return OrderValidationResult.insufficientStock(
                    order.getId(),
                    stockValidationResult.getInsufficientItems()
            );
        }
        //5. 적용한 쿠폰 삽입
        order.addCoupon(couponId);

        // 5. 쿠폰 적용 및 최종 금액 계산
        BigDecimal discount = calculateDiscount(order, userId);
        Money finalAmount = order.getTotalPrice().subtract(Money.of(discount));

        // 6. 주문에 할인 금액 반영
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            order.applyDiscount(discount);
            orderRepository.save(order);
            log.info("할인 적용 완료 - orderId: {}, discount: {}", order.getId(), discount);
        }

        log.info("주문 검증 완료 - orderId: {}, finalAmount: {}", order.getId(), finalAmount);

        return OrderValidationResult.success(
                order.getId(),
                order.getId(),
                finalAmount.getAmount(),
                discount
        );
    }

    /* 쿠폰 할인 계산 */
    private BigDecimal calculateDiscount(Order order, Long userId) {
        if (!order.hasCoupon()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal discount = memberCouponService.calculateDiscount(
                    userId,
                    order.getCouponId(),
                    order.getTotalPrice().getAmount()
            );

            log.info("쿠폰 할인 계산 완료 - orderId: {}, couponId: {}, discount: {}",
                    order.getId(), order.getCouponId(), discount);

            return discount;

        } catch (Exception e) {
            log.error("쿠폰 할인 계산 실패 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId(), e);
            throw new BusinessException(ErrorCode.COUPON_NOT_FOUND,
                    "쿠폰 적용에 실패했습니다: " + e.getMessage());
        }
    }

    private void rollbackCouponIfUsed(Order order) {
        if (!order.hasCoupon()) {
            log.debug("쿠폰 없음 - 쿠폰 롤백 스킵");
            return;
        }

        try {
            memberCouponService.rollbackMemberCoupon(
                    order.getUserId(),
                    order.getCouponId(),
                    order.getId()
            );

            log.info("✓ 쿠폰 복구 완료 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId());

        } catch (Exception e) {
            log.error("쿠폰 복구 실패 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId(), e);


            throw new BusinessException(
                    ErrorCode.COUPON_ROLLBACK_INVALID,
                    "쿠폰 복구에 실패했습니다: " + e.getMessage()
            );
        }
    }

    private void validateStock(Map<ProductOption, Integer> optionsWithQuantity) {
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

        List<ProductOption> productOptions = productOptionRepository.findAllById(optionIds);

        //ProductOptionId 유효성 확인
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
