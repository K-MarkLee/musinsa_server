package com.mudosa.musinsa.product.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOptionValue is a Querydsl query type for OptionValue
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOptionValue extends EntityPathBase<OptionValue> {

    private static final long serialVersionUID = -1367405924L;

    public static final QOptionValue optionValue1 = new QOptionValue("optionValue1");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath optionName = createString("optionName");

    public final StringPath optionValue = createString("optionValue");

    public final NumberPath<Long> optionValueId = createNumber("optionValueId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOptionValue(String variable) {
        super(OptionValue.class, forVariable(variable));
    }

    public QOptionValue(Path<? extends OptionValue> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOptionValue(PathMetadata metadata) {
        super(OptionValue.class, metadata);
    }

}

