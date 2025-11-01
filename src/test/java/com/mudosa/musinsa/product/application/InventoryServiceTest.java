package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.repository.InventoryRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("재고 수량을 추가하면 값이 증가한다")
    void addStock_increaseQuantity() {
        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(5))
            .build();

        when(inventoryRepository.findByProductOptionIdWithLock(anyLong()))
            .thenReturn(Optional.of(inventory));

        inventoryService.addStock(1L, 3);

        assertThat(inventory.getStockQuantity().getValue()).isEqualTo(8);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("음수나 0을 추가하면 예외가 발생한다")
    void addStock_invalidQuantity() {
        assertThatThrownBy(() -> inventoryService.addStock(1L, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

}
