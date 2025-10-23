package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    
    @Column(name = "event_id")
    private Long eventId;
    
    @Column(name = "event_option_id")
    private Long eventOptionId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price"))
    })
    private Money unitPrice;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_price"))
    })
    private Money totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_scope")
    private LimitScope limitScope;
    
    @Column(name = "paid_flag", nullable = false)
    private Boolean paidFlag = false;
    
    /**
     * 주문 상품 생성
     */
    public static OrderProduct create(
        Long userId,
        Long productId,
        Long productOptionId,
        Long eventId,
        Long eventOptionId,
        int quantity,
        Money unitPrice,
        LimitScope limitScope
    ) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.userId = userId;
        orderProduct.productId = productId;
        orderProduct.productOptionId = productOptionId;
        orderProduct.eventId = eventId;
        orderProduct.eventOptionId = eventOptionId;
        orderProduct.quantity = quantity;
        orderProduct.unitPrice = unitPrice;
        orderProduct.totalPrice = unitPrice.multiply(quantity);
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
}
