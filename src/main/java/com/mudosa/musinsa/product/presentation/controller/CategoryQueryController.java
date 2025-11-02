package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 디버깅 목적: 카테고리 ID 기반으로 buildPath 결과를 확인하는 임시 엔드포인트
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryQueryController {

    private final CategoryRepository categoryRepository;

    @GetMapping("/{categoryId}/path")
    public ResponseEntity<CategoryPathResponse> getCategoryPath(@PathVariable Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));

    return ResponseEntity.ok(new CategoryPathResponse(categoryId, category.buildPath()));
    }

    @Value
    private static class CategoryPathResponse {
        Long categoryId;
        String categoryPath;
    }
}
