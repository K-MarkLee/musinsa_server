package com.mudosa.musinsa.order.application.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.mudosa.musinsa.order.application.dto.QPendingOrderItem is a Querydsl Projection type for PendingOrderItem
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPendingOrderItem extends ConstructorExpression<PendingOrderItem> {

    private static final long serialVersionUID = -679500025L;

    public QPendingOrderItem(com.querydsl.core.types.Expression<Long> productOptionId, com.querydsl.core.types.Expression<String> brandName, com.querydsl.core.types.Expression<String> productOptionName, com.querydsl.core.types.Expression<? extends java.math.BigDecimal> amount, com.querydsl.core.types.Expression<Integer> quantity, com.querydsl.core.types.Expression<String> imageUrl, com.querydsl.core.types.Expression<String> size, com.querydsl.core.types.Expression<String> color) {
        super(PendingOrderItem.class, new Class<?>[]{long.class, String.class, String.class, java.math.BigDecimal.class, int.class, String.class, String.class, String.class}, productOptionId, brandName, productOptionName, amount, quantity, imageUrl, size, color);
    }

}

