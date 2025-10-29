package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.coupon.domain.service.MemberCouponService;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.product.application.CartService;
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
    private final CartService cartService;
    private final MemberCouponService memberCouponService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void completeOrder(Long orderId) {
        log.info("주문 완료 처리 시작 - orderId: {}", orderId);

        /* 주문 조회 및 검증 */
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        //주문 상태에 대한 검증
        order.validatePending();

        //주문 상품의 상태에 대한 검증
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
        List<Long> productOptionsIds = order.getOrderProducts().stream()
                        .map(orderProduct -> {
                            return orderProduct.getProductOption().getProductOptionId();
                        }).toList();

        cartService.deleteCartItemsByProductOptions(order.getUserId(), productOptionsIds);

        log.info("장바구니 삭제 완료 - orderId: {}, userId: {}", orderId, order.getUserId());

        /* 쿠폰을 사용한 주문이라면 MemberCoupon 사용 처리 */
       if(order.hasCoupon()){
           try{
               memberCouponService.useMemberCoupon(order.getUserId(), order.getCouponId(), orderId);
               log.info("쿠폰 사용 처리 완료 - orderId: {}, couponId: {}",
                       orderId, order.getCouponId());
           }catch (Exception e){
               //그냥 로그 찍으려고 에러 던진 거기 때문에 쿠폰 사용 실패는 주문 완료 트랜잭션에 영향이 없어야함.
               log.error("쿠폰 사용 처리 실패 - orderId: {}, couponId: {}, error: {}",
                       orderId, order.getCouponId(), e.getMessage());
           }

       }
        
        log.info("주문 완료 처리 성공 - orderId: {}", orderId);
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
        log.warn("주문 롤백 완료 - orderId: {}, status: PENDING", orderId);
    }
}
