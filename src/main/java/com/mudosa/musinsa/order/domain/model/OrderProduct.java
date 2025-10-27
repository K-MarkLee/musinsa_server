package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주문 상품 엔티티
 * Order 애그리거트 내부
 */
@Entity
@Table(name = "order_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_option_id", nullable = false)
    private Long productOptionId;
    
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice; // 구매 시점 가격 스냅샷
    
    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity = 1;
    
    @Column(name = "event_id")
    private Long eventId; // 이벤트를 통한 구매
    
    @Column(name = "event_option_id")
    private Long eventOptionId;
    
    @Column(name = "paid_flag", nullable = false)
    private Boolean paidFlag = false; // 결제 완료 여부
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_scope")
    private LimitScope limitScope;
    
    /**
     * 주문 상품 생성
     */
    public static OrderProduct create(
        Long userId,
        Long productId,
        Long productOptionId,
        BigDecimal productPrice,
        Integer productQuantity,
        Long eventId,
        Long eventOptionId,
        LimitScope limitScope
    ) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.userId = userId;
        orderProduct.productId = productId;
        orderProduct.productOptionId = productOptionId;
        orderProduct.productPrice = productPrice;
        orderProduct.productQuantity = productQuantity;
        orderProduct.eventId = eventId;
        orderProduct.eventOptionId = eventOptionId;
        orderProduct.limitScope = limitScope;
        orderProduct.paidFlag = false;
        return orderProduct;
    }
    
    /**
     * Order 할당 (Package Private)
     */
    void assignOrder(Order order) {
        this.order = order;
    }
    
    /**
     * 결제 완료 처리
     */
    public void markAsPaid() {
        this.paidFlag = true;
    }
    
    /**
     * 총 가격 계산
     */
    public BigDecimal getTotalPrice() {
        return productPrice.multiply(BigDecimal.valueOf(productQuantity));
    }
}
