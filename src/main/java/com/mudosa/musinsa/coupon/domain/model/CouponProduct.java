package com.mudosa.musinsa.coupon.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠폰 적용 상품 엔티티
 * Coupon 애그리거트 내부
 */
@Entity
@Table(name = "coupon_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponProduct extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_product_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    
    @Column(name = "product_id", nullable = false)
    private Long productId; // Product 애그리거트 참조 (ID만)
    
    /**
     * 쿠폰 상품 생성
     */
    public static CouponProduct create(Long productId) {
        CouponProduct couponProduct = new CouponProduct();
        couponProduct.productId = productId;
        return couponProduct;
    }
    
    /**
     * Coupon 할당 (Package Private)
     */
    void assignCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}
