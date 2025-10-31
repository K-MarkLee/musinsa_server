package com.mudosa.musinsa.order.application.dto;

import java.math.BigDecimal;

public record OrderMemberCoupon(
        Long couponId,
        String couponName,
        String discountType,
        BigDecimal discountValue
) {
}
