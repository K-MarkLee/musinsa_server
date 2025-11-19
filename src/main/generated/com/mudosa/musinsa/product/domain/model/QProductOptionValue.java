package com.mudosa.musinsa.product.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductOptionValue is a Querydsl query type for ProductOptionValue
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOptionValue extends EntityPathBase<ProductOptionValue> {

    private static final long serialVersionUID = 830665965L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductOptionValue productOptionValue = new QProductOptionValue("productOptionValue");

    public final QProductOptionValue_ProductOptionValueId id;

    public final QOptionValue optionValue;

    public final QProductOption productOption;

    public QProductOptionValue(String variable) {
        this(ProductOptionValue.class, forVariable(variable), INITS);
    }

    public QProductOptionValue(Path<? extends ProductOptionValue> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductOptionValue(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductOptionValue(PathMetadata metadata, PathInits inits) {
        this(ProductOptionValue.class, metadata, inits);
    }

    public QProductOptionValue(Class<? extends ProductOptionValue> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.id = inits.isInitialized("id") ? new QProductOptionValue_ProductOptionValueId(forProperty("id")) : null;
        this.optionValue = inits.isInitialized("optionValue") ? new QOptionValue(forProperty("optionValue")) : null;
        this.productOption = inits.isInitialized("productOption") ? new QProductOption(forProperty("productOption"), inits.get("productOption")) : null;
    }

}

