package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.product.application.ProductService;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductCreateResponse;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchRequest;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
// 상품 API 초안 컨트롤러로 검색, 생성, 상세 조회 엔드포인트를 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    // 검색 조건을 받아 상품 목록을 조회한다.
    @GetMapping
    public ResponseEntity<ProductSearchResponse> searchProducts(@Valid ProductSearchRequest request) {
        ProductSearchResponse response = productService.searchProducts(request.toCondition());
        return ResponseEntity.ok(response);
    }

    // 관리자용 상품 생성 요청을 처리하고 생성된 리소스 위치를 반환한다.
    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("브랜드를 찾을 수 없습니다. brandId=" + request.getBrandId()));
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("카테고리를 찾을 수 없습니다. categoryId=" + request.getCategoryId()));

        Long productId = productService.createProduct(request, brand, category);
        URI location = URI.create("/api/products/" + productId);
        return ResponseEntity.created(location)
            .body(ProductCreateResponse.builder().productId(productId).build());
    }

    // 상품 식별자를 기준으로 상세 정보를 조회한다.
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse response = productService.getProductDetail(productId);
        return ResponseEntity.ok(response);
    }

    // 브랜드 멤버가 상품 기본 정보를 수정한다.
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> updateProduct(@PathVariable Long productId,
                                                               @RequestHeader("X-USER-ID") Long userId,
                                                               @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse response = productService.updateProduct(productId, userId, request);
        return ResponseEntity.ok(response);
    }

    // 브랜드 멤버가 상품을 비활성화(소프트 삭제) 처리한다.
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId,
                                              @RequestHeader("X-USER-ID") Long userId) {
        productService.disableProduct(productId, userId);
        return ResponseEntity.noContent().build();
    }
}
