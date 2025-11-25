package com.mudosa.musinsa.order.application.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.mudosa.musinsa.order.application.dto.QOrderMemberCoupon is a Querydsl Projection type for OrderMemberCoupon
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QOrderMemberCoupon extends ConstructorExpression<OrderMemberCoupon> {

    private static final long serialVersionUID = -63594991L;

    public QOrderMemberCoupon(com.querydsl.core.types.Expression<Long> couponId, com.querydsl.core.types.Expression<String> couponName, com.querydsl.core.types.Expression<com.mudosa.musinsa.coupon.domain.model.DiscountType> discountType, com.querydsl.core.types.Expression<? extends java.math.BigDecimal> discountValue) {
        super(OrderMemberCoupon.class, new Class<?>[]{long.class, String.class, com.mudosa.musinsa.coupon.domain.model.DiscountType.class, java.math.BigDecimal.class}, couponId, couponName, discountType, discountValue);
    }

}

