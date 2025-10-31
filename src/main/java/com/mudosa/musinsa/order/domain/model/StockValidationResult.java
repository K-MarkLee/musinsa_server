package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockValidationResult {
    private final boolean isValid;
    private final List<InsufficientStockItem> insufficientItems;

    public static StockValidationResult valid() {
        return new StockValidationResult(true, Collections.emptyList());
    }

    public static StockValidationResult invalid(
            List<InsufficientStockItem> insufficientItems) {
        return new StockValidationResult(false, insufficientItems);
    }

    public boolean hasInsufficientStock() {
        return !isValid;
    }
}
