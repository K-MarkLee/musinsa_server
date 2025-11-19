package com.mudosa.musinsa.product.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStockQuantity is a Querydsl query type for StockQuantity
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QStockQuantity extends BeanPath<StockQuantity> {

    private static final long serialVersionUID = 507374215L;

    public static final QStockQuantity stockQuantity = new QStockQuantity("stockQuantity");

    public final NumberPath<Integer> value = createNumber("value", Integer.class);

    public QStockQuantity(String variable) {
        super(StockQuantity.class, forVariable(variable));
    }

    public QStockQuantity(Path<? extends StockQuantity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStockQuantity(PathMetadata metadata) {
        super(StockQuantity.class, metadata);
    }

}

