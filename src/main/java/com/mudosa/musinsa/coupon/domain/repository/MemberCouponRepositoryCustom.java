package com.mudosa.musinsa.coupon.domain.repository;

import com.mudosa.musinsa.order.application.dto.OrderMemberCoupon;

import java.util.List;

public interface MemberCouponRepositoryCustom {
    List<OrderMemberCoupon> findOrderMemberCouponsByUserId(Long userId);
}
