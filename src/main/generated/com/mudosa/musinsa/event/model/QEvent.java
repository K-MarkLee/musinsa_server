package com.mudosa.musinsa.event.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEvent is a Querydsl query type for Event
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEvent extends EntityPathBase<Event> {

    private static final long serialVersionUID = 1843024273L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEvent event = new QEvent("event");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final com.mudosa.musinsa.coupon.domain.model.QCoupon coupon;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endedAt = createDateTime("endedAt", java.time.LocalDateTime.class);

    public final ListPath<EventImage, QEventImage> eventImages = this.<EventImage, QEventImage>createList("eventImages", EventImage.class, QEventImage.class, PathInits.DIRECT2);

    public final ListPath<EventOption, QEventOption> eventOptions = this.<EventOption, QEventOption>createList("eventOptions", EventOption.class, QEventOption.class, PathInits.DIRECT2);

    public final EnumPath<Event.EventType> eventType = createEnum("eventType", Event.EventType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPublic = createBoolean("isPublic");

    public final NumberPath<Integer> limitPerUser = createNumber("limitPerUser", Integer.class);

    public final EnumPath<Event.LimitScope> limitScope = createEnum("limitScope", Event.LimitScope.class);

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final EnumPath<EventStatus> status = createEnum("status", EventStatus.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEvent(String variable) {
        this(Event.class, forVariable(variable), INITS);
    }

    public QEvent(Path<? extends Event> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEvent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEvent(PathMetadata metadata, PathInits inits) {
        this(Event.class, metadata, inits);
    }

    public QEvent(Class<? extends Event> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new com.mudosa.musinsa.coupon.domain.model.QCoupon(forProperty("coupon")) : null;
    }

}

