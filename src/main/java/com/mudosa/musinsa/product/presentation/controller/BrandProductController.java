package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.ProductCommandService;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductCreateResponse;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;

// 브랜드 관리자가 상품을 생성, 수정, 삭제하고 옵션을 관리하는 엔드포인트를 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands/{brandId}/products")
public class BrandProductController {

    private final ProductCommandService productService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(@PathVariable Long brandId,
                                                               @Valid @RequestBody ProductCreateRequest request) {
        if (!Objects.equals(brandId, request.getBrandId())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "요청 경로의 브랜드와 본문이 일치하지 않습니다.");
        }

        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new EntityNotFoundException("브랜드를 찾을 수 없습니다. brandId=" + brandId));
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다. categoryId=" + request.getCategoryId()));

        Long productId = productService.createProduct(request, brand, category);
        URI location = URI.create(String.format("/api/brands/%d/products/%d", brandId, productId));
        return ResponseEntity.created(location)
            .body(ProductCreateResponse.builder().productId(productId).build());
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> updateProduct(@PathVariable Long brandId,
                                                               @PathVariable Long productId,
                                                               @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse response = productService.updateProduct(brandId, productId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/options")
    public ResponseEntity<ProductDetailResponse.OptionDetail> addProductOption(@PathVariable Long brandId,
                                                                               @PathVariable Long productId,
                                                                               @Valid @RequestBody ProductOptionCreateRequest request) {
        ProductDetailResponse.OptionDetail response = productService.addProductOption(brandId, productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{productId}/options/{productOptionId}")
    public ResponseEntity<Void> deleteProductOption(@PathVariable Long brandId,
                                                    @PathVariable Long productId,
                                                    @PathVariable Long productOptionId) {
        productService.removeProductOption(brandId, productId, productOptionId);
        return ResponseEntity.noContent().build();
    }
}
