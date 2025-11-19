package com.mudosa.musinsa.event.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventImage is a Querydsl query type for EventImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventImage extends EntityPathBase<EventImage> {

    private static final long serialVersionUID = 1586936394L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventImage eventImage = new QEventImage("eventImage");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QEvent event;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isThumbnail = createBoolean("isThumbnail");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventImage(String variable) {
        this(EventImage.class, forVariable(variable), INITS);
    }

    public QEventImage(Path<? extends EventImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventImage(PathMetadata metadata, PathInits inits) {
        this(EventImage.class, metadata, inits);
    }

    public QEventImage(Class<? extends EventImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.event = inits.isInitialized("event") ? new QEvent(forProperty("event"), inits.get("event")) : null;
    }

}

