package com.mudosa.musinsa.fbtoken.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFirebaseToken is a Querydsl query type for FirebaseToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFirebaseToken extends EntityPathBase<FirebaseToken> {

    private static final long serialVersionUID = 869941452L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFirebaseToken firebaseToken = new QFirebaseToken("firebaseToken");

    public final StringPath firebaseTokenKey = createString("firebaseTokenKey");

    public final NumberPath<Long> tokenId = createNumber("tokenId", Long.class);

    public final com.mudosa.musinsa.user.domain.model.QUser user;

    public QFirebaseToken(String variable) {
        this(FirebaseToken.class, forVariable(variable), INITS);
    }

    public QFirebaseToken(Path<? extends FirebaseToken> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFirebaseToken(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFirebaseToken(PathMetadata metadata, PathInits inits) {
        this(FirebaseToken.class, metadata, inits);
    }

    public QFirebaseToken(Class<? extends FirebaseToken> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.mudosa.musinsa.user.domain.model.QUser(forProperty("user")) : null;
    }

}

