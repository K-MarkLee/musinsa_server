package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.product.domain.model.CartItem;
import com.mudosa.musinsa.product.domain.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;

    /* 장바구니 삭제 */
    @Transactional
    public void deleteCartItemsByProductOptions(Long userId, List<Long> productOptionIds) {
        if (productOptionIds == null || productOptionIds.isEmpty()) {
            log.info("삭제할 장바구니 아이템 없음 - userId: {}", userId);
            return;
        }

        log.info("장바구니 삭제 시작 - userId: {}, productOptionIds: {}",
                userId, productOptionIds);

        int deletedCount = cartItemRepository.deleteByUserIdAndProductOptionIdIn(
                userId,
                productOptionIds
        );

        log.info("장바구니 삭제 완료 - userId: {}, deletedCount: {}",
                userId, deletedCount);
    }
}
