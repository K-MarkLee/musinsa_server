package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.coupon.domain.service.MemberCouponService;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.domain.model.Orders;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.payment.application.dto.OrderValidationResult;
import com.mudosa.musinsa.product.application.CartService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MemberCouponService memberCouponService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeOrder(Long orderId) {
        log.info("주문 완료 처리 시작 - orderId: {}", orderId);

        /* 주문 조회 및 검증 */
        Orders orders = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        /* 주문 상태 재검증 -> 동시성 제어를 위해 */
        orders.validatePending();

        /* 주문 상품 재검증 */
        orders.validateOrderProducts();

        log.info("주문 검증 완료 - orderId: {}, orderProducts: {}",
                orderId, orders.getOrderProducts().size());

        /* 재고 차감 */
        orders.decreaseStock();
        log.info("재고 차감 완료 - orderId: {}", orderId);

        /* 주문 상태 변경 */
        orders.complete();
        orderRepository.save(orders);

        log.info("주문 상태 변경 완료 - orderId: {}, status: COMPLETED", orderId);

        /* 주문 제품에 대한 장바구니 제품 삭제 */
        deleteCartItems(orders);

        log.info("장바구니 삭제 완료 - orderId: {}, userId: {}", orderId, orders.getUserId());

        /* 쿠폰을 사용한 주문이라면 MemberCoupon 사용 처리 */
        useCouponIfExists(orders);
        
        log.info("주문 완료 처리 성공 - orderId: {}", orderId);
    }

    private void deleteCartItems(Orders order) {
        List<Long> productOptionIds = order.getOrderProducts().stream()
                .map(op -> op.getProductOption().getProductOptionId())
                .toList();

        cartService.deleteCartItemsByProductOptions(order.getUserId(), productOptionIds);

        log.info("장바구니 삭제 완료 - orderId: {}, count: {}",
                order.getId(), productOptionIds.size());
    }

    private void useCouponIfExists(Orders order) {
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
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        orders.restoreStock();
        log.info("재고 복구 완료 - orderId: {}", orderId);

        /* 주문 상태 복구 */
        orders.rollback();
        orderRepository.save(orders);

        /* 쿠폰 복구 */
        rollbackCouponIfUsed(orders);

        log.warn("주문 롤백 완료 - orderId: {}, status: PENDING", orderId);
    }

    public boolean isOrderCompleted(Long orderId) {
        return orderRepository.findById(orderId).map(order -> order.getStatus().isCompleted()).orElse(false);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderValidationResult validateAndPrepareOrder(
            String orderNo,
            Long userId,
            BigDecimal requestAmount) {

        log.info("주문 검증 시작 - orderNo: {}, userId: {}, requestAmount: {}",
                orderNo, userId, requestAmount);

        // 1. 주문 조회
        Orders order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderNo));

        // 2. 사용자 권한 검증
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER,
                    "본인의 주문만 결제할 수 있습니다");
        }

        // 3. 주문 상태 검증
        order.validatePending();

        // 4. 주문 상품 검증
        order.validateOrderProducts();

        // 5. 쿠폰 적용 및 최종 금액 계산
        BigDecimal discount = calculateDiscount(order, userId);
        BigDecimal finalAmount = order.getTotalPrice().subtract(discount);

        // 6. 주문에 할인 금액 반영
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            order.applyDiscount(discount);
            orderRepository.save(order);
            log.info("할인 적용 완료 - orderId: {}, discount: {}", order.getId(), discount);
        }


        log.info("주문 검증 완료 - orderId: {}, finalAmount: {}", order.getId(), finalAmount);

        return OrderValidationResult.of(
                order.getId(),
                order.getUserId(),
                finalAmount,
                discount
        );
    }

    /* 쿠폰 할인 계산 */
    private BigDecimal calculateDiscount(Orders order, Long userId) {
        if (!order.hasCoupon()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal discount = memberCouponService.calculateDiscount(
                    userId,
                    order.getCouponId(),
                    order.getTotalPrice()
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

    private void rollbackCouponIfUsed(Orders order) {
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

            // TODO:쿠폰 복구 못했다고 결제를 취소해야되나...?
            throw new BusinessException(
                    ErrorCode.COUPON_ROLLBACK_INVALID,
                    "쿠폰 복구에 실패했습니다: " + e.getMessage()
            );
        }
    }
}
