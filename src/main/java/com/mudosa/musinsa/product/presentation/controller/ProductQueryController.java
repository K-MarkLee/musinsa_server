package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.product.application.ProductService;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchRequest;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 사용자용 상품 검색과 상세 조회 엔드포인트를 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductQueryController {

    private final ProductService productService;

    // 검색 조건을 받아 상품 목록을 조회한다.
    @GetMapping
    public ResponseEntity<ProductSearchResponse> searchProducts(@Valid ProductSearchRequest request) {
        ProductSearchResponse response = productService.searchProducts(request.toCondition());
        return ResponseEntity.ok(response);
    }

    // 상품 식별자를 기준으로 상세 정보를 조회한다.
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);
        return ResponseEntity.ok(response);
    }
}
