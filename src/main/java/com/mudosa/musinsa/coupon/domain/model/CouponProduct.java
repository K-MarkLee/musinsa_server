package com.mudosa.musinsa.coupon.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long productId;

    public static CouponProduct create(Long productId) {
        CouponProduct couponProduct = new CouponProduct();
        couponProduct.productId = productId;
        return couponProduct;
    }
    void assignCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}
