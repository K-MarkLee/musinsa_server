package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// 상품 검색을 위한 커스텀 리포지토리 인터페이스이다.
public interface ProductRepositoryCustom {

    // 조건값을 기반으로 상품 목록을 필터링해 반환한다.
    List<Product> findAllByFilters(List<String> categoryPaths,
                                   ProductGenderType gender,
                                   String keyword, // TODO: 검색 확장시 분리.
                                   Long brandId);

    // 조건값과 페이징 정보를 기반으로 상품 목록을 데이터베이스 레벨에서 필터링, 정렬, 페이징하여 반환한다.
    Page<Product> findAllByFiltersWithPagination(List<String> categoryPaths,
                                                ProductGenderType gender,
                                                String keyword,
                                                Long brandId,
                                                ProductSearchCondition.PriceSort priceSort,
                                                Pageable pageable);
}
