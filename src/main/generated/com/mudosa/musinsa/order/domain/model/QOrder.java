package com.mudosa.musinsa.order.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = 548103983L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final com.mudosa.musinsa.common.domain.model.QBaseEntity _super = new com.mudosa.musinsa.common.domain.model.QBaseEntity(this);

    public final NumberPath<Long> couponId = createNumber("couponId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isSettleable = createBoolean("isSettleable");

    public final StringPath orderNo = createString("orderNo");

    public final ListPath<OrderProduct, QOrderProduct> orderProducts = this.<OrderProduct, QOrderProduct>createList("orderProducts", OrderProduct.class, QOrderProduct.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> settledAt = createDateTime("settledAt", java.time.LocalDateTime.class);

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    public final com.mudosa.musinsa.common.vo.QMoney totalDiscount;

    public final com.mudosa.musinsa.common.vo.QMoney totalPrice;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.totalDiscount = inits.isInitialized("totalDiscount") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalDiscount")) : null;
        this.totalPrice = inits.isInitialized("totalPrice") ? new com.mudosa.musinsa.common.vo.QMoney(forProperty("totalPrice")) : null;
    }

}

