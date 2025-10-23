package com.mudosa.musinsa.coupon.domain.repository;

import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MemberCoupon Repository
 */
@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    
    List<MemberCoupon> findByUserId(Long userId);
    
    List<MemberCoupon> findByUserIdAndIsUsedFalse(Long userId);
}
