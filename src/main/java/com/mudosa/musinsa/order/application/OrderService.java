package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.coupon.domain.repository.CouponRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final CouponRepository couponRepository;
    // TODO: private final CartService cartService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void completeOrder(Long orderId) {
        log.info("주문 완료 처리 시작 - orderId: {}", orderId);
        
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        
        // 2. 주문 상태 검증 (PENDING이어야 함)
        order.validatePending();
        
        // 3. 주문 상품 조회
        List<OrderProduct> orderProducts = order.getOrderProducts();
        if (orderProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        
//        // 4. 재고 차감 (InventoryService에 위임)
//        for (OrderProduct orderProduct : orderProducts) {
//            Long productOptionId = orderProduct.getProductOptionId();
//            Integer quantity = orderProduct.getQuantity();
//
//            // InventoryService가 비관적 락으로 재고 차감
//            inventoryService.deduct(productOptionId, quantity);
//        }
        
        // 5. 주문 상태 COMPLETED로 변경
        order.complete();
        orderRepository.save(order);
        
        log.info("주문 상태 변경 완료 - orderId: {}, status: COMPLETED", orderId);
        
//        // 6. 장바구니 삭제 (TODO)
//         if (isFromCart) {
//             List<Long> productOptionIds = orderProducts.stream()
//                 .map(OrderProduct::getProductOptionId)
//                 .toList();
//             cartService.deleteCartItems(order.getUserId(), productOptionIds);
//         }
//
//        // 7. 쿠폰 사용 처리 (TODO)
//         if (order.getCouponId() != null) {
//             UserCoupon coupon = couponRepository.findById(order.getCouponId());
//             coupon.use();
//             couponRepository.save(coupon);
//         }
        
        log.info("주문 완료 처리 성공 - orderId: {}", orderId);
    }
    

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackOrder(Long orderId) {
        log.warn("주문 롤백 시작 - orderId: {}", orderId);
        
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        
        // 2. 주문 상품 조회
        List<OrderProduct> orderProducts = order.getOrderProducts();
        
//        // 3. 재고 복구 (InventoryService에 위임)
//        for (OrderProduct orderProduct : orderProducts) {
//            Long productOptionId = orderProduct.getProductOptionId();
//            Integer quantity = orderProduct.getQuantity();
//
//            // InventoryService가 재고 복구
//            inventoryService.restore(productOptionId, quantity);
//        }
        
        // 4. 주문 상태 PENDING으로 원복
        order.rollback();
        orderRepository.save(order);
        
        log.warn("주문 롤백 완료 - orderId: {}", orderId);
    }
}
