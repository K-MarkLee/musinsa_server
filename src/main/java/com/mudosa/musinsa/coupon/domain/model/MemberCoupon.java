package com.mudosa.musinsa.coupon.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_status", nullable = false)
    private CouponStatus couponStatus = CouponStatus.AVAILABLE;
    
    @Column(name = "used_order_id")
    private Long usedOrderId;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    public void validateUsable() {
        if (this.couponStatus == CouponStatus.USED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (this.couponStatus == CouponStatus.EXPIRED) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        if (this.expiredAt != null && LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
    }
    

    public void use(Long orderId) {
        validateUsable();
        
        this.couponStatus = CouponStatus.USED;
        this.usedOrderId = orderId;
        this.usedAt = LocalDateTime.now();
    }
    

    public void expire() {
        if (this.couponStatus == CouponStatus.USED) {
            throw new IllegalStateException("이미 사용된 쿠폰은 만료 처리할 수 없습니다.");
        }
        this.couponStatus = CouponStatus.EXPIRED;
    }
    

    public boolean canUse() {
        return couponStatus == CouponStatus.AVAILABLE && 
               (expiredAt == null || LocalDateTime.now().isBefore(expiredAt));
    }
}

