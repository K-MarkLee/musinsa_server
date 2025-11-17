package com.mudosa.musinsa.order.application.dto;

import com.mudosa.musinsa.coupon.domain.model.DiscountType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderMemberCoupon{
    private Long couponId;
    private String couponName;
    private DiscountType discountType;
    private BigDecimal discountValue;

    @QueryProjection
    public OrderMemberCoupon(Long couponId, String couponName, DiscountType discountType, BigDecimal discountValue) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
    }
}
