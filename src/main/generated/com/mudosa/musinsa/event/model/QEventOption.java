package com.mudosa.musinsa.event.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventOption is a Querydsl query type for EventOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventOption extends EntityPathBase<EventOption> {

    private static final long serialVersionUID = 2125501798L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventOption eventOption = new QEventOption("eventOption");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QEvent event;

    public final NumberPath<java.math.BigDecimal> eventPrice = createNumber("eventPrice", java.math.BigDecimal.class);

    public final NumberPath<Integer> eventStock = createNumber("eventStock", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.mudosa.musinsa.product.domain.model.QProductOption productOption;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventOption(String variable) {
        this(EventOption.class, forVariable(variable), INITS);
    }

    public QEventOption(Path<? extends EventOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventOption(PathMetadata metadata, PathInits inits) {
        this(EventOption.class, metadata, inits);
    }

    public QEventOption(Class<? extends EventOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new QEvent(forProperty("event"), inits.get("event")) : null;
        this.productOption = inits.isInitialized("productOption") ? new com.mudosa.musinsa.product.domain.model.QProductOption(forProperty("productOption"), inits.get("productOption")) : null;
    }

}

