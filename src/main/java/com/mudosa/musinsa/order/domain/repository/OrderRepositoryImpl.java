package com.mudosa.musinsa.order.domain.repository;

import com.mudosa.musinsa.order.application.dto.PendingOrderItem;
import com.mudosa.musinsa.order.application.dto.QPendingOrderItem;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mudosa.musinsa.brand.domain.model.QBrand.brand;
import static com.mudosa.musinsa.order.domain.model.QOrder.order;
import static com.mudosa.musinsa.order.domain.model.QOrderProduct.orderProduct;
import static com.mudosa.musinsa.product.domain.model.QImage.image;
import static com.mudosa.musinsa.product.domain.model.QOptionValue.optionValue1;
import static com.mudosa.musinsa.product.domain.model.QProduct.product;
import static com.mudosa.musinsa.product.domain.model.QProductOption.productOption;
import static com.mudosa.musinsa.product.domain.model.QProductOptionValue.productOptionValue;


@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PendingOrderItem> findOrderItems(String orderNo) {

        StringExpression sizeValue = new CaseBuilder()
                .when(optionValue1.optionName.eq("SIZE"))
                .then(optionValue1.optionValue)
                .otherwise((String) null);

        StringExpression colorValue = new CaseBuilder()
                .when(optionValue1.optionName.eq("COLOR"))
                .then(optionValue1.optionValue)
                .otherwise((String) null);

        JPQLQuery<String> imageOne = JPAExpressions
                .select(image.imageUrl)
                .from(image)
                .where(image.product.eq(product)
                        .and(image.isThumbnail.eq(true)))
                .orderBy(image.imageId.asc())
                .limit(1);

        return queryFactory
                .select(new QPendingOrderItem(
                        productOption.productOptionId,
                        brand.nameKo,
                        product.productName,
                        productOption.productPrice.amount,
                        orderProduct.productQuantity,
                        imageOne,
                        sizeValue.max(),
                        colorValue.max()
                ))
                .from(order)
                .join(order.orderProducts, orderProduct)
                .join(orderProduct.productOption, productOption)
                .join(productOption.product, product)
                .join(product.brand, brand)
                .join(productOption.productOptionValues, productOptionValue)
                .join(productOptionValue.optionValue, optionValue1)
                .where(order.orderNo.eq(orderNo)
                        .and(optionValue1.optionName.in("SIZE", "COLOR")))
                .groupBy(
                        productOption.productOptionId,
                        brand.nameKo,
                        product.productName,
                        productOption.productPrice.amount,
                        orderProduct.productQuantity
                )
                .fetch();
    }
}
