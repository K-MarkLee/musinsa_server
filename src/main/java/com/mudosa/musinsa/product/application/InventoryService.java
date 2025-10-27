package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 재고 서비스
 * 
 * 책임:
 * - 재고 차감
 * - 재고 복구
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 차감 (비관적 락)
     * 
     * @param productOptionId 상품 옵션 ID
     * @param quantity 차감 수량
     * @throws BusinessException INVENTORY_NOT_FOUND, INSUFFICIENT_STOCK
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void deduct(Long productOptionId, Integer quantity) {
        log.info("재고 차감 시작 - productOptionId: {}, quantity: {}", 
            productOptionId, quantity);
        
        // 비관적 락으로 재고 조회
        Inventory inventory = inventoryRepository.findByProductOptionIdWithLock(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
        
        // 재고 차감
        try {
            inventory.decrease(quantity);
            inventoryRepository.save(inventory);
            
            log.info("재고 차감 완료 - productOptionId: {}, 차감 수량: {}, 남은 재고: {}", 
                productOptionId, quantity, inventory.getStockQuantity());
            
        } catch (IllegalStateException e) {
            log.error("재고 부족 - productOptionId: {}, 요청 수량: {}, 현재 재고: {}", 
                productOptionId, quantity, inventory.getStockQuantity());
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    /**
     * 재고 복구
     * 
     * @param productOptionId 상품 옵션 ID
     * @param quantity 복구 수량
     * @throws BusinessException INVENTORY_NOT_FOUND
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void restore(Long productOptionId, Integer quantity) {
        log.info("재고 복구 시작 - productOptionId: {}, quantity: {}", 
            productOptionId, quantity);
        
        // 재고 조회
        Inventory inventory = inventoryRepository.findByProductOptionId(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
        
        // 재고 복구
        inventory.increase(quantity);
        inventoryRepository.save(inventory);
        
        log.info("재고 복구 완료 - productOptionId: {}, 복구 수량: {}, 복구 후 재고: {}", 
            productOptionId, quantity, inventory.getStockQuantity());
    }
}
