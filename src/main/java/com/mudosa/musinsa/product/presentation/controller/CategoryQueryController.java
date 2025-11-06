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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/tree")
    public ResponseEntity<CategoryTreeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        
        // 부모 카테고리만 필터링 (parent가 null인 것들)
        List<Category> parentCategories = allCategories.stream()
            .filter(category -> category.getParent() == null)
            .collect(Collectors.toList());
        
        // 자식 카테고리 맵 생성
        Map<Long, List<Category>> childrenMap = allCategories.stream()
            .filter(category -> category.getParent() != null)
            .collect(Collectors.groupingBy(category -> category.getParent().getCategoryId()));
        
        // 트리 구조로 변환
        List<CategoryNode> categoryNodes = parentCategories.stream()
            .map(parent -> {
                List<Category> children = childrenMap.getOrDefault(parent.getCategoryId(), List.of());
                return CategoryNode.from(parent, children);
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new CategoryTreeResponse(categoryNodes));
    }

    @Value
    private static class CategoryPathResponse {
        Long categoryId;
        String categoryPath;
    }

    @Value
    private static class CategoryTreeResponse {
        List<CategoryNode> categories;
    }

    @Value
    private static class CategoryNode {
        Long categoryId;
        String categoryName;
        String categoryPath;
        String imageUrl;
        List<CategoryNode> children;
        
        static CategoryNode from(Category category, List<Category> children) {
            List<CategoryNode> childNodes = children.stream()
                .map(child -> new CategoryNode(
                    child.getCategoryId(),
                    child.getCategoryName(),
                    child.buildPath(),
                    child.getImageUrl(),
                    List.of() // 손주는 없으므로 빈 리스트
                ))
                .collect(Collectors.toList());
                
            return new CategoryNode(
                category.getCategoryId(),
                category.getCategoryName(),
                category.buildPath(),
                category.getImageUrl(),
                childNodes
            );
        }
    }
}
