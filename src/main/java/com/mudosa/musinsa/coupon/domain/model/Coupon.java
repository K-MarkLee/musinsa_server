package com.mudosa.musinsa.coupon.domain.model;

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
 * 쿠폰 애그리거트 루트
 */
@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;
    
    @Column(name = "coupon_name", nullable = false, length = 100, unique = true)
    private String couponName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "total_quantity")
    private Integer totalQuantity;
    
    @Column(name = "issued_quantity", nullable = false)
    private Integer issuedQuantity = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // 쿠폰 상품 (같은 애그리거트)
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouponProduct> couponProducts = new ArrayList<>();
    
    /**
     * 쿠폰 생성
     */
    public static Coupon create(
        String couponName,
        DiscountType discountType,
        BigDecimal discountValue,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer totalQuantity
    ) {
        Coupon coupon = new Coupon();
        coupon.couponName = couponName;
        coupon.discountType = discountType;
        coupon.discountValue = discountValue;
        coupon.startDate = startDate;
        coupon.endDate = endDate;
        coupon.totalQuantity = totalQuantity;
        coupon.issuedQuantity = 0;
        coupon.isActive = true;
        return coupon;
    }
    
    /**
     * 쿠폰 상품 추가
     */
    public void addCouponProduct(CouponProduct couponProduct) {
        this.couponProducts.add(couponProduct);
        couponProduct.assignCoupon(this);
    }
    
    /**
     * 쿠폰 발급
     */
    public void issue() {
        if (!canIssue()) {
            throw new IllegalStateException("발급할 수 없는 쿠폰입니다.");
        }
        this.issuedQuantity++;
    }
    
    /**
     * 쿠폰 발급 가능 여부
     */
    public boolean canIssue() {
        if (!isActive) return false;
        if (totalQuantity != null && issuedQuantity >= totalQuantity) return false;
        
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}
