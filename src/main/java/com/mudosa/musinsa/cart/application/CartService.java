package com.mudosa.musinsa.cart.application;

import com.mudosa.musinsa.cart.domain.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 장바구니 서비스
 * 
 * 책임:
 * - 장바구니 아이템 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;

    /**
     * 장바구니 아이템 삭제 (주문 완료 후)
     * 
     * @param userId 사용자 ID
     * @param productOptionIds 상품 옵션 ID 리스트
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteCartItems(Long userId, List<Long> productOptionIds) {
        log.info("장바구니 아이템 삭제 - userId: {}, count: {}", userId, productOptionIds.size());
        
        cartItemRepository.deleteByUserIdAndProductOptionIdIn(userId, productOptionIds);
        
        log.info("장바구니 아이템 삭제 완료 - userId: {}", userId);
    }
}
