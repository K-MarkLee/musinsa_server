package com.mudosa.musinsa.common.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCreatedOnlyEntity is a Querydsl query type for CreatedOnlyEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QCreatedOnlyEntity extends EntityPathBase<CreatedOnlyEntity> {

    private static final long serialVersionUID = -269327173L;

    public static final QCreatedOnlyEntity createdOnlyEntity = new QCreatedOnlyEntity("createdOnlyEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public QCreatedOnlyEntity(String variable) {
        super(CreatedOnlyEntity.class, forVariable(variable));
    }

    public QCreatedOnlyEntity(Path<? extends CreatedOnlyEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCreatedOnlyEntity(PathMetadata metadata) {
        super(CreatedOnlyEntity.class, metadata);
    }

}

