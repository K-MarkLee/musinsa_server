package com.mudosa.musinsa.settlement.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlementYearly is a Querydsl query type for SettlementYearly
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlementYearly extends EntityPathBase<SettlementYearly> {

    private static final long serialVersionUID = -492190131L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlementYearly settlementYearly = new QSettlementYearly("settlementYearly");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> aggregatedAt = createDateTime("aggregatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.mudosa.musinsa.common.vo.QMoney finalSettlementAmount;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath settlementNumber = createString("settlementNumber");

    public final EnumPath<SettlementStatus> settlementStatus = createEnum("settlementStatus", SettlementStatus.class);

    public final StringPath settlementTimezone = createString("settlementTimezone");

    public final NumberPath<Integer> settlementYear = createNumber("settlementYear", Integer.class);

    public final com.mudosa.musinsa.common.vo.QMoney totalCommissionAmount;

    public final NumberPath<Integer> totalOrderCount = createNumber("totalOrderCount", Integer.class);

    public final com.mudosa.musinsa.common.vo.QMoney totalPgFeeAmount;

    public final com.mudosa.musinsa.common.vo.QMoney totalSalesAmount;

    public final com.mudosa.musinsa.common.vo.QMoney totalTaxAmount;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DatePath<java.time.LocalDate> yearEndDate = createDate("yearEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> yearStartDate = createDate("yearStartDate", java.time.LocalDate.class);

    public QSettlementYearly(String variable) {
        this(SettlementYearly.class, forVariable(variable), INITS);
    }

    public QSettlementYearly(Path<? extends SettlementYearly> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlementYearly(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlementYearly(PathMetadata metadata, PathInits inits) {
        this(SettlementYearly.class, metadata, inits);
    }

    public QSettlementYearly(Class<? extends SettlementYearly> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.finalSettlementAmount = inits.isInitialized("finalSettlementAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("finalSettlementAmount")) : null;
        this.totalCommissionAmount = inits.isInitialized("totalCommissionAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalCommissionAmount")) : null;
        this.totalPgFeeAmount = inits.isInitialized("totalPgFeeAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalPgFeeAmount")) : null;
        this.totalSalesAmount = inits.isInitialized("totalSalesAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalSalesAmount")) : null;
        this.totalTaxAmount = inits.isInitialized("totalTaxAmount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalTaxAmount")) : null;
    }

}

