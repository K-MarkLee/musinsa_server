package com.mudosa.musinsa.coupon.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCouponProduct is a Querydsl query type for CouponProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCouponProduct extends EntityPathBase<CouponProduct> {

    private static final long serialVersionUID = -1999459886L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCouponProduct couponProduct = new QCouponProduct("couponProduct");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final QCoupon coupon;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCouponProduct(String variable) {
        this(CouponProduct.class, forVariable(variable), INITS);
    }

    public QCouponProduct(Path<? extends CouponProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCouponProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCouponProduct(PathMetadata metadata, PathInits inits) {
        this(CouponProduct.class, metadata, inits);
    }

    public QCouponProduct(Class<? extends CouponProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QCoupon(forProperty("coupon")) : null;
    }

}

