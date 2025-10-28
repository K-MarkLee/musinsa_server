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
    private Long userId;
    
    @Column(name = "coupon_id")
    private Long couponId;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus;
    
    @Column(name = "order_no", nullable = false, length = 50, unique = true)
    private String orderNo;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "total_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    
    @Column(name = "final_payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPaymentAmount;
    
    @Column(name = "is_settleable", nullable = false)
    private Boolean isSettleable = false;
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();
    

    public void validatePending() {
        if (this.orderStatus != 1) {
            throw new IllegalStateException("이미 처리된 주문입니다. 현재 상태: " + this.orderStatus);
        }
    }

    public void complete() {
        this.orderStatus = 2; // COMPLETED
        this.isSettleable = true; // 정산 가능하도록 설정
    }

    public void rollback() {
        this.orderStatus = 1; // PENDING
        this.isSettleable = false;
    }

}
