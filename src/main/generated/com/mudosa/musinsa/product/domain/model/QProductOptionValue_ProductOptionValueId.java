package com.mudosa.musinsa.product.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductOptionValue_ProductOptionValueId is a Querydsl query type for ProductOptionValueId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QProductOptionValue_ProductOptionValueId extends BeanPath<ProductOptionValue.ProductOptionValueId> {

    private static final long serialVersionUID = 1549096457L;

    public static final QProductOptionValue_ProductOptionValueId productOptionValueId = new QProductOptionValue_ProductOptionValueId("productOptionValueId");

    public final NumberPath<Long> optionValueId = createNumber("optionValueId", Long.class);

    public final NumberPath<Long> productOptionId = createNumber("productOptionId", Long.class);

    public QProductOptionValue_ProductOptionValueId(String variable) {
        super(ProductOptionValue.ProductOptionValueId.class, forVariable(variable));
    }

    public QProductOptionValue_ProductOptionValueId(Path<? extends ProductOptionValue.ProductOptionValueId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductOptionValue_ProductOptionValueId(PathMetadata metadata) {
        super(ProductOptionValue.ProductOptionValueId.class, metadata);
    }

}

