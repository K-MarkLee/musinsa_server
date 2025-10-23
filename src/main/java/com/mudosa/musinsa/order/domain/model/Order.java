package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId; // Brand 애그리거트 참조 (ID만)
    
    @Column(name = "coupon_id")
    private Long couponId; // Coupon 애그리거트 참조 (ID만)
    
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus; // StatusCode FK
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount"))
    })
    private Money totalAmount;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "discount_amount"))
    })
    private Money discountAmount;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "final_amount"))
    })
    private Money finalAmount;
    
    @Column(name = "recipient_name", nullable = false, length = 50)
    private String recipientName;
    
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;
    
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;
    
    @Column(name = "shipping_request", length = 255)
    private String shippingRequest;
    
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
        Money totalAmount,
        Money discountAmount,
        Money finalAmount,
        String recipientName,
        String recipientPhone,
        String shippingAddress
    ) {
        Order order = new Order();
        order.userId = userId;
        order.brandId = brandId;
        order.couponId = couponId;
        order.orderStatus = orderStatus;
        order.totalAmount = totalAmount;
        order.discountAmount = discountAmount;
        order.finalAmount = finalAmount;
        order.recipientName = recipientName;
        order.recipientPhone = recipientPhone;
        order.shippingAddress = shippingAddress;
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
}
