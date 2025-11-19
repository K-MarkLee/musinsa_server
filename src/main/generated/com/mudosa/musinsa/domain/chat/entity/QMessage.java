package com.mudosa.musinsa.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessage is a Querydsl query type for Message
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessage extends EntityPathBase<Message> {

    private static final long serialVersionUID = -511215464L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessage message = new QMessage("message");

    public final ListPath<MessageAttachment, QMessageAttachment> attachments = this.<MessageAttachment, QMessageAttachment>createList("attachments", MessageAttachment.class, QMessageAttachment.class, PathInits.DIRECT2);

    public final NumberPath<Long> chatId = createNumber("chatId", Long.class);

    public final QChatPart chatPart;

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> messageId = createNumber("messageId", Long.class);

    public final QMessage parent;

    public QMessage(String variable) {
        this(Message.class, forVariable(variable), INITS);
    }

    public QMessage(Path<? extends Message> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessage(PathMetadata metadata, PathInits inits) {
        this(Message.class, metadata, inits);
    }

    public QMessage(Class<? extends Message> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatPart = inits.isInitialized("chatPart") ? new QChatPart(forProperty("chatPart"), inits.get("chatPart")) : null;
        this.parent = inits.isInitialized("parent") ? new QMessage(forProperty("parent"), inits.get("parent")) : null;
    }

}

