package com.mudosa.musinsa.coupon.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 애그리거트 루트
 * - Coupon과 별도 애그리거트 (사용자별 쿠폰 발급 정보)
 */
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
    private Long userId; // User 애그리거트 참조 (ID만)
    
    @Column(name = "coupon_id", nullable = false)
    private Long couponId; // Coupon 애그리거트 참조 (ID만)
    
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
    
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
    
    /**
     * 사용자 쿠폰 발급
     */
    public static MemberCoupon issue(Long userId, Long couponId, LocalDateTime expiredAt) {
        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.userId = userId;
        memberCoupon.couponId = couponId;
        memberCoupon.issuedAt = LocalDateTime.now();
        memberCoupon.expiredAt = expiredAt;
        memberCoupon.isUsed = false;
        return memberCoupon;
    }
    
    /**
     * 쿠폰 사용
     */
    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
    
    /**
     * 사용 가능 여부
     */
    public boolean canUse() {
        return !isUsed && LocalDateTime.now().isBefore(expiredAt);
    }
}
