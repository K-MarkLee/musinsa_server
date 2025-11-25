package com.mudosa.musinsa.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatPart is a Querydsl query type for ChatPart
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatPart extends EntityPathBase<ChatPart> {

    private static final long serialVersionUID = -1063413510L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatPart chatPart = new QChatPart("chatPart");

    public final NumberPath<Long> chatPartId = createNumber("chatPartId", Long.class);

    public final QChatRoom chatRoom;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final ListPath<Message, QMessage> messages = this.<Message, QMessage>createList("messages", Message.class, QMessage.class, PathInits.DIRECT2);

    public final EnumPath<com.mudosa.musinsa.domain.chat.enums.ChatPartRole> role = createEnum("role", com.mudosa.musinsa.domain.chat.enums.ChatPartRole.class);

    public final com.mudosa.musinsa.user.domain.model.QUser user;

    public QChatPart(String variable) {
        this(ChatPart.class, forVariable(variable), INITS);
    }

    public QChatPart(Path<? extends ChatPart> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatPart(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatPart(PathMetadata metadata, PathInits inits) {
        this(ChatPart.class, metadata, inits);
    }

    public QChatPart(Class<? extends ChatPart> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom"), inits.get("chatRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.mudosa.musinsa.user.domain.model.QUser(forProperty("user")) : null;
    }

}

