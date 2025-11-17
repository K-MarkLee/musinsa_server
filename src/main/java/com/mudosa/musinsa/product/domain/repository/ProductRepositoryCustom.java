package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// 상품 검색을 위한 커스텀 리포지토리 인터페이스이다.
public interface ProductRepositoryCustom {

    // 필터링 조건값과 페이징 정보를 기반으로 상품 목록을 QueryDSL로 필터링, 정렬, 페이징하여 반환한다. (키워드 제외)
    Page<Product> findAllByFiltersWithPagination(List<String> categoryPaths,
                                                ProductGenderType gender,
                                                Long brandId,
                                                ProductSearchCondition.PriceSort priceSort,
                                                Pageable pageable);

    // 키워드를 기반으로 상품을 Full-Text Search로 검색하고 추가 필터링 조건을 QueryDSL로 적용하여 반환한다.
    Page<Product> searchByKeywordWithFilters(String keyword,
                                           List<String> categoryPaths,
                                           ProductGenderType gender,
                                           Long brandId,
                                           ProductSearchCondition.PriceSort priceSort,
                                           Pageable pageable);
}
