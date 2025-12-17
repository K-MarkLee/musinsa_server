package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mudosa.musinsa.product.domain.model.QInventory.inventory;
import static com.mudosa.musinsa.product.domain.model.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class ProductOptionRepositoryImpl implements ProductOptionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProductOption> findByProductOptionIdInWithPessimisticLock(List<Long> productOptionIds) {
        return jpaQueryFactory
                .selectFrom(productOption)
                .join(productOption.inventory, inventory).fetchJoin()
                .where(productOption.productOptionId.in(productOptionIds))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }

    @Override
    public List<ProductOption> findByProductOptionIdIn(List<Long> productOptionIds) {
        return jpaQueryFactory
                .selectFrom(productOption)
                .join(productOption.inventory, inventory).fetchJoin()
                .where(productOption.productOptionId.in(productOptionIds))
                .fetch();
    }
}
