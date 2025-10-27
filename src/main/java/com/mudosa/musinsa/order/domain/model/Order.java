package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 애그리거트 루트
 */
@Entity
@Table(name = "`order`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // User 애그리거트 참조 (ID만)
    
    @Column(name = "coupon_id")
    private Long couponId; // Coupon 애그리거트 참조 (ID만)
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId; // Brand 애그리거트 참조 (ID만)
    
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus; // StatusCode FK
    
    @Column(name = "order_no", nullable = false, length = 50, unique = true)
    private String orderNo; // 외부 노출용 주문번호
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // 주문 총액 (할인 전)
    
    @Column(name = "total_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO; // 총 할인 금액
    
    @Column(name = "final_payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPaymentAmount; // 최종 결제 금액
    
    @Column(name = "is_settleable", nullable = false)
    private Boolean isSettleable = false; // 정산 가능 여부
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt; // 정산 완료 일시
    
    // 주문 상품 (같은 애그리거트)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();
    
    /**
     * 주문 생성
     */
    public static Order create(
        Long userId,
        Long brandId,
        Long couponId,
        Integer orderStatus,
        String orderNo,
        BigDecimal totalPrice,
        BigDecimal totalDiscount,
        BigDecimal finalPaymentAmount
    ) {
        Order order = new Order();
        order.userId = userId;
        order.brandId = brandId;
        order.couponId = couponId;
        order.orderStatus = orderStatus;
        order.orderNo = orderNo;
        order.totalPrice = totalPrice;
        order.totalDiscount = totalDiscount;
        order.finalPaymentAmount = finalPaymentAmount;
        order.isSettleable = false;
        return order;
    }
    
    /**
     * 주문 상품 추가
     */
    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
        orderProduct.assignOrder(this);
    }
    
    /**
     * 주문 상태 변경
     */
    public void changeStatus(Integer newStatus) {
        this.orderStatus = newStatus;
    }
    
    /**
     * 주문 상태가 PENDING인지 검증
     */
    public void validatePending() {
        // TODO: OrderStatus enum 또는 상수로 관리 필요
        // 임시로 1 = PENDING, 2 = COMPLETED로 가정
        if (this.orderStatus != 1) {
            throw new IllegalStateException("이미 처리된 주문입니다. 현재 상태: " + this.orderStatus);
        }
    }
    
    /**
     * 주문 완료 처리 (상태를 COMPLETED로 변경)
     */
    public void complete() {
        this.orderStatus = 2; // COMPLETED
        this.isSettleable = true; // 정산 가능하도록 설정
    }
    
    /**
     * 주문 롤백 (상태를 PENDING으로 원복)
     */
    public void rollback() {
        this.orderStatus = 1; // PENDING
        this.isSettleable = false;
    }
    
    /**
     * 정산 가능하도록 설정
     */
    public void markAsSettleable() {
        this.isSettleable = true;
    }
    
    /**
     * 정산 완료 처리
     */
    public void settle() {
        if (!this.isSettleable) {
            throw new IllegalStateException("정산 가능한 주문이 아닙니다.");
        }
        this.settledAt = LocalDateTime.now();
    }
}
