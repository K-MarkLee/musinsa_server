package com.mudosa.musinsa.product.domain.repository;

import java.util.List;

public interface CartItemRepositoryCustom {
    void deleteByUserIdAndProductOptionIdIn(
           Long userId,
           List<Long> productOptionIds
    );
}
