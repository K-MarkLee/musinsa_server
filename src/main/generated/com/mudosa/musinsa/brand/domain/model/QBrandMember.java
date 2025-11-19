package com.mudosa.musinsa.brand.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBrandMember is a Querydsl query type for BrandMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBrandMember extends EntityPathBase<BrandMember> {

    private static final long serialVersionUID = 441889289L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBrandMember brandMember = new QBrandMember("brandMember");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final QBrand brand;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QBrandMember(String variable) {
        this(BrandMember.class, forVariable(variable), INITS);
    }

    public QBrandMember(Path<? extends BrandMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBrandMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBrandMember(PathMetadata metadata, PathInits inits) {
        this(BrandMember.class, metadata, inits);
    }

    public QBrandMember(Class<? extends BrandMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.brand = inits.isInitialized("brand") ? new QBrand(forProperty("brand")) : null;
    }

}

