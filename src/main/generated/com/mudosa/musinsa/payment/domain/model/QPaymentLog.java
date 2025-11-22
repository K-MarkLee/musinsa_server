package com.mudosa.musinsa.payment.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentLog is a Querydsl query type for PaymentLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentLog extends EntityPathBase<PaymentLog> {

    private static final long serialVersionUID = -982237483L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentLog paymentLog = new QPaymentLog("paymentLog");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath eventMessage = createString("eventMessage");

    public final EnumPath<PaymentEventType> eventStatus = createEnum("eventStatus", PaymentEventType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPayment payment;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPaymentLog(String variable) {
        this(PaymentLog.class, forVariable(variable), INITS);
    }

    public QPaymentLog(Path<? extends PaymentLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentLog(PathMetadata metadata, PathInits inits) {
        this(PaymentLog.class, metadata, inits);
    }

    public QPaymentLog(Class<? extends PaymentLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payment = inits.isInitialized("payment") ? new QPayment(forProperty("payment")) : null;
    }

}

