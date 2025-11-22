package com.mudosa.musinsa.settlement.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlementPerTransaction is a Querydsl query type for SettlementPerTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlementPerTransaction extends EntityPathBase<SettlementPerTransaction> {

    private static final long serialVersionUID = -1275609532L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlementPerTransaction settlementPerTransaction = new QSettlementPerTransaction("settlementPerTransaction");

    public final com.mudosa.musinsa.common.domain.model.QCreatedOnlyEntity _super = new com.mudosa.musinsa.common.domain.model.QCreatedOnlyEntity(this);

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final com.mudosa.musinsa.common.vo.QMoney commissionAmount;

    public final NumberPath<java.math.BigDecimal> commissionRate = createNumber("commissionRate", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final com.mudosa.musinsa.common.vo.QMoney pgFeeAmount;

    public final StringPath pgTransactionId = createString("pgTransactionId");

    public final com.mudosa.musinsa.common.vo.QMoney taxAmount;

    public final StringPath timezoneOffset = createString("timezoneOffset");

    public final com.mudosa.musinsa.common.vo.QMoney transactionAmount;

    public final DateTimePath<java.time.LocalDateTime> transactionDate = createDateTime("transactionDate", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> transactionDateLocal = createDate("transactionDateLocal", java.time.LocalDate.class);

    public final EnumPath<TransactionType> transactionType = createEnum("transactionType", TransactionType.class);

    public QSettlementPerTransaction(String variable) {
        this(SettlementPerTransaction.class, forVariable(variable), INITS);
    }

    public QSettlementPerTransaction(Path<? extends SettlementPerTransaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlementPerTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlementPerTransaction(PathMetadata metadata, PathInits inits) {
        this(SettlementPerTransaction.class, metadata, inits);
    }

    public QSettlementPerTransaction(Class<? extends SettlementPerTransaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.commissionAmount = inits.isInitialized("commissionAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("commissionAmount")) : null;
        this.pgFeeAmount = inits.isInitialized("pgFeeAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("pgFeeAmount")) : null;
        this.taxAmount = inits.isInitialized("taxAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("taxAmount")) : null;
        this.transactionAmount = inits.isInitialized("transactionAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("transactionAmount")) : null;
    }

}

