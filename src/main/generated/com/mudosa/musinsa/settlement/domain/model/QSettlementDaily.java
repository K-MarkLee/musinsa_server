package com.mudosa.musinsa.settlement.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlementDaily is a Querydsl query type for SettlementDaily
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlementDaily extends EntityPathBase<SettlementDaily> {

    private static final long serialVersionUID = 934448630L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlementDaily settlementDaily = new QSettlementDaily("settlementDaily");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> aggregatedAt = createDateTime("aggregatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.mudosa.musinsa.common.vo.QMoney finalSettlementAmount;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> settlementDate = createDate("settlementDate", java.time.LocalDate.class);

    public final StringPath settlementNumber = createString("settlementNumber");

    public final EnumPath<SettlementStatus> settlementStatus = createEnum("settlementStatus", SettlementStatus.class);

    public final StringPath settlementTimezone = createString("settlementTimezone");

    public final com.mudosa.musinsa.common.vo.QMoney totalCommissionAmount;

    public final NumberPath<Integer> totalOrderCount = createNumber("totalOrderCount", Integer.class);

    public final com.mudosa.musinsa.common.vo.QMoney totalPgFeeAmount;

    public final com.mudosa.musinsa.common.vo.QMoney totalSalesAmount;

    public final com.mudosa.musinsa.common.vo.QMoney totalTaxAmount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSettlementDaily(String variable) {
        this(SettlementDaily.class, forVariable(variable), INITS);
    }

    public QSettlementDaily(Path<? extends SettlementDaily> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlementDaily(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlementDaily(PathMetadata metadata, PathInits inits) {
        this(SettlementDaily.class, metadata, inits);
    }

    public QSettlementDaily(Class<? extends SettlementDaily> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.finalSettlementAmount = inits.isInitialized("finalSettlementAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("finalSettlementAmount")) : null;
        this.totalCommissionAmount = inits.isInitialized("totalCommissionAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalCommissionAmount")) : null;
        this.totalPgFeeAmount = inits.isInitialized("totalPgFeeAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalPgFeeAmount")) : null;
        this.totalSalesAmount = inits.isInitialized("totalSalesAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalSalesAmount")) : null;
        this.totalTaxAmount = inits.isInitialized("totalTaxAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalTaxAmount")) : null;
    }

}

