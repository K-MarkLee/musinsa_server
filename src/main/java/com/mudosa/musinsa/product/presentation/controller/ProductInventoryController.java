package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.product.application.ProductInventoryService;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 브랜드 관리자용 재고 API를 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands/{brandId}/products/{productId}")
public class ProductInventoryController {

    private final ProductInventoryService productInventoryService;

    // 브랜드별 상품 옵션 재고 목록 조회
    @GetMapping("/inventory")
    public ResponseEntity<List<ProductOptionStockResponse>> getProductOptionStocks(@PathVariable Long brandId,
                                                                                   @PathVariable Long productId) {
        List<ProductOptionStockResponse> response = productInventoryService.getProductOptionStocks(brandId, productId);
        return ResponseEntity.ok(response);
    }

    // 상품 옵션 재고 추가 (입고)
    @PostMapping("/inventory/increase")
    public ResponseEntity<ProductOptionStockResponse> increaseStock(@PathVariable Long brandId,
                                                                    @PathVariable Long productId,
                                                                    @Valid @RequestBody StockAdjustmentRequest request) {
        ProductOptionStockResponse response = productInventoryService.addStock(brandId, productId, request);
        return ResponseEntity.ok(response);
    }

    // 상품 옵션 재고 차감 (출고)
    @PostMapping("/inventory/decrease")
    public ResponseEntity<ProductOptionStockResponse> decreaseStock(@PathVariable Long brandId,
                                                                    @PathVariable Long productId,
                                                                    @Valid @RequestBody StockAdjustmentRequest request) {
        ProductOptionStockResponse response = productInventoryService.subtractStock(brandId, productId, request);
        return ResponseEntity.ok(response);
    }

    // 상품 전체 판매 가능 상태 변경
    @PatchMapping("/availability")
    public ResponseEntity<ProductAvailabilityResponse> changeProductAvailability(@PathVariable Long brandId,
                                                                                 @PathVariable Long productId,
                                                                                 @Valid @RequestBody ProductAvailabilityRequest request) {
        ProductAvailabilityResponse response = productInventoryService.updateProductAvailability(brandId, productId, request);
        return ResponseEntity.ok(response);
    }
}
