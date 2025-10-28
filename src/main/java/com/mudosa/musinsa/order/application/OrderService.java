package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.product.application.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final MemberCouponRepository memberCouponRepository;
//    private final CartService cartService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void completeOrder(Long orderId, boolean isFromCart) {
        log.info("주문 완료 처리 시작 - orderId: {}, isFromCart: {}", orderId, isFromCart);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        
        order.validatePending();
        
        List<OrderProduct> orderProducts = order.getOrderProducts();
        if (orderProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        
        for (OrderProduct orderProduct : orderProducts) {
            Long productOptionId = orderProduct.getProductOptionId();
            Integer quantity = orderProduct.getProductQuantity();
            
            inventoryService.deduct(productOptionId, quantity);
        }
        
        order.complete();
        orderRepository.save(order);
        
        log.info("주문 상태 변경 완료 - orderId: {}, status: COMPLETED", orderId);
        
        if (isFromCart) {
            List<Long> productOptionIds = orderProducts.stream()
                .map(OrderProduct::getProductOptionId)
                .collect(Collectors.toList());
            
//            cartService.deleteCartItems(order.getUserId(), productOptionIds);
            
            log.info("장바구니 삭제 완료 - orderId: {}, userId: {}", orderId, order.getUserId());
        }
        
        if (order.getCouponId() != null) {
            MemberCoupon memberCoupon = memberCouponRepository
                .findByUserIdAndCouponId(order.getUserId(), order.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
            
            memberCoupon.use(orderId);
            memberCouponRepository.save(memberCoupon);
            
            log.info("쿠폰 사용 처리 완료 - orderId: {}, couponId: {}", orderId, order.getCouponId());
        }
        
        log.info("주문 완료 처리 성공 - orderId: {}", orderId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void completeOrder(Long orderId) {
        completeOrder(orderId, false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackOrder(Long orderId) {
        log.warn("주문 롤백 시작 - orderId: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        
        List<OrderProduct> orderProducts = order.getOrderProducts();
        
        for (OrderProduct orderProduct : orderProducts) {
            Long productOptionId = orderProduct.getProductOptionId();
            Integer quantity = orderProduct.getProductQuantity();
            
            inventoryService.restore(productOptionId, quantity);
        }
        
        order.rollback();
        orderRepository.save(order);
        
        log.warn("주문 롤백 완료 - orderId: {}", orderId);
    }
}
