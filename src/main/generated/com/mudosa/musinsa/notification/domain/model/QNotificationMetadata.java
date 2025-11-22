package com.mudosa.musinsa.notification.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationMetadata is a Querydsl query type for NotificationMetadata
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationMetadata extends EntityPathBase<NotificationMetadata> {

    private static final long serialVersionUID = 95052982L;

    public static final QNotificationMetadata notificationMetadata = new QNotificationMetadata("notificationMetadata");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> nMetadataId = createNumber("nMetadataId", Integer.class);

    public final StringPath notificationCategory = createString("notificationCategory");

    public final StringPath notificationMessage = createString("notificationMessage");

    public final StringPath notificationTitle = createString("notificationTitle");

    public final StringPath notificationType = createString("notificationType");

    public final StringPath notificationUrl = createString("notificationUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QNotificationMetadata(String variable) {
        super(NotificationMetadata.class, forVariable(variable));
    }

    public QNotificationMetadata(Path<? extends NotificationMetadata> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationMetadata(PathMetadata metadata) {
        super(NotificationMetadata.class, metadata);
    }

}

