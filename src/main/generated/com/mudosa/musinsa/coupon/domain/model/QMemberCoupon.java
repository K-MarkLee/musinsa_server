package com.mudosa.musinsa.coupon.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberCoupon is a Querydsl query type for MemberCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberCoupon extends EntityPathBase<MemberCoupon> {

    private static final long serialVersionUID = 1477598807L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberCoupon memberCoupon = new QMemberCoupon("memberCoupon");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final QCoupon coupon;

    public final EnumPath<CouponStatus> couponStatus = createEnum("couponStatus", CouponStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> usedAt = createDateTime("usedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> usedOrderId = createNumber("usedOrderId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QMemberCoupon(String variable) {
        this(MemberCoupon.class, forVariable(variable), INITS);
    }

    public QMemberCoupon(Path<? extends MemberCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberCoupon(PathMetadata metadata, PathInits inits) {
        this(MemberCoupon.class, metadata, inits);
    }

    public QMemberCoupon(Class<? extends MemberCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QCoupon(forProperty("coupon")) : null;
    }

}

