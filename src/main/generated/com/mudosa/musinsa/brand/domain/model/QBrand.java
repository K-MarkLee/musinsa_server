package com.mudosa.musinsa.brand.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBrand is a Querydsl query type for Brand
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBrand extends EntityPathBase<Brand> {

    private static final long serialVersionUID = -2032819569L;

    public static final QBrand brand = new QBrand("brand");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final ListPath<BrandMember, QBrandMember> brandMembers = this.<BrandMember, QBrandMember>createList("brandMembers", BrandMember.class, QBrandMember.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> commissionRate = createNumber("commissionRate", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath logoUrl = createString("logoUrl");

    public final StringPath nameEn = createString("nameEn");

    public final StringPath nameKo = createString("nameKo");

    public final EnumPath<BrandStatus> status = createEnum("status", BrandStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QBrand(String variable) {
        super(Brand.class, forVariable(variable));
    }

    public QBrand(Path<? extends Brand> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBrand(PathMetadata metadata) {
        super(Brand.class, metadata);
    }

}

