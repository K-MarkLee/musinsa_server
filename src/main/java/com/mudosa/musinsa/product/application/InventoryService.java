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

// 재고 차감과 복구를 담당하는 서비스이다.
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // 지정된 수량만큼 옵션 재고를 차감한다.
    @Transactional(propagation = Propagation.MANDATORY)
    public void deduct(Long productOptionId, Integer quantity) {
        log.info("재고 차감 시작 - productOptionId: {}, quantity: {}",
            productOptionId, quantity);

        // TODO 주문/결제 흐름 확정 시 차감 로직을 재활성화한다.
    }

    // 지정된 수량만큼 옵션 재고를 복구한다. (결제 취소 및 주문 취소) - 환불은 없음
    @Transactional(propagation = Propagation.MANDATORY)
    public void restore(Long productOptionId, Integer quantity) {
        log.info("재고 복구 시작 - productOptionId: {}, quantity: {}",
            productOptionId, quantity);

        // TODO 주문/결제 취소 연동 시 복구 로직을 재활성화한다.
    }

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

    // 재고 수량을 직접 덮어쓴다.
    @Transactional(propagation = Propagation.MANDATORY)
    public void overrideStock(Long productOptionId, Integer quantity) {
        log.info("재고 수량 덮어쓰기 - productOptionId: {}, quantity: {}",
            productOptionId, quantity);

        if (quantity == null || quantity < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Inventory inventory = loadInventoryWithLock(productOptionId);

        try {
            inventory.overrideQuantity(quantity);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, e.getMessage());
        }

        inventoryRepository.save(inventory);

        log.info("재고 수량 갱신 완료 - productOptionId: {}, 현재 재고: {}",
            productOptionId, inventory.getStockQuantity());
    }

    // 재고 판매 가능 상태를 직접 변경한다.
    @Transactional(propagation = Propagation.MANDATORY)
    public void changeAvailability(Long productOptionId, Boolean available) {
        log.info("재고 판매 가능 상태 변경 - productOptionId: {}, available: {}",
            productOptionId, available);

        if (available == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Inventory inventory = loadInventoryWithLock(productOptionId);

        try {
            inventory.changeAvailability(available);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, e.getMessage());
        }

        inventoryRepository.save(inventory);

        log.info("재고 판매 가능 상태 변경 완료 - productOptionId: {}, available: {}",
            productOptionId, inventory.getIsAvailable());
    }

    private Inventory loadInventoryWithLock(Long productOptionId) {
        return inventoryRepository.findByProductOptionIdWithLock(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
    }
}
