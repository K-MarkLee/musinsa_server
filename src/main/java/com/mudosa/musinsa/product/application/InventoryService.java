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

// 재고 증감과 모니터링을 담당하는 서비스이다.
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // 지정된 수량만큼 옵션 재고를 추가한다 (입고/재입고).
    @Transactional(propagation = Propagation.MANDATORY)
    public void addStock(Long productOptionId, Integer quantity) {
        log.info("재고 추가 시작 - productOptionId: {}, quantity: {}",
            productOptionId, quantity);

        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Inventory inventory = loadInventoryWithLock(productOptionId);

        inventory.increase(quantity);
        inventoryRepository.save(inventory);

        log.info("재고 추가 완료 - productOptionId: {}, 추가 수량: {}, 현재 재고: {}",
            productOptionId, quantity, inventory.getStockQuantity());
    }

    private Inventory loadInventoryWithLock(Long productOptionId) {
        return inventoryRepository.findByProductOptionIdWithLock(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
    }
}