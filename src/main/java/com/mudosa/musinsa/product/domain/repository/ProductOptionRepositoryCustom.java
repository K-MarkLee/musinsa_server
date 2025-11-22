package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.ProductOption;

import java.util.List;

public interface ProductOptionRepositoryCustom {
    List<ProductOption> findByProductOptionIdInWithPessimisticLock(List<Long> ProductOption);
    List<ProductOption> findByProductOptionIdIn(List<Long> productOptionIds);
}
