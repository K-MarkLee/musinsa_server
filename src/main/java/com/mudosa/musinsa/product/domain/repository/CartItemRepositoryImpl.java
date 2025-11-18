package com.mudosa.musinsa.product.domain.repository;

import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mudosa.musinsa.product.domain.model.QCartItem.cartItem;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom{
    private final JPQLQueryFactory jpqlQueryFactory;

    @Override
    public void deleteByUserIdAndProductOptionIdIn(Long userId, List<Long> productOptionIds) {
        jpqlQueryFactory
                .delete(cartItem)
                .where(
                        cartItem.user.id.eq(userId)
                                .and(cartItem.productOption.productOptionId.in(productOptionIds))
                );
    }
}
