package com.mudosa.musinsa.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessageAttachment is a Querydsl query type for MessageAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessageAttachment extends EntityPathBase<MessageAttachment> {

    private static final long serialVersionUID = -2111516709L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessageAttachment messageAttachment = new QMessageAttachment("messageAttachment");

    public final NumberPath<Long> attachmentId = createNumber("attachmentId", Long.class);

    public final StringPath attachmentUrl = createString("attachmentUrl");

    public final QMessage message;

    public final StringPath mimeType = createString("mimeType");

    public final NumberPath<Long> sizeBytes = createNumber("sizeBytes", Long.class);

    public QMessageAttachment(String variable) {
        this(MessageAttachment.class, forVariable(variable), INITS);
    }

    public QMessageAttachment(Path<? extends MessageAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessageAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessageAttachment(PathMetadata metadata, PathInits inits) {
        this(MessageAttachment.class, metadata, inits);
    }

    public QMessageAttachment(Class<? extends MessageAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.message = inits.isInitialized("message") ? new QMessage(forProperty("message"), inits.get("message")) : null;
    }

}

