package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;

import java.util.List;

// 상품 검색을 위한 커스텀 리포지토리 인터페이스이다.
public interface ProductRepositoryCustom {

    // 조건값을 기반으로 상품 목록을 필터링해 반환한다.
    List<Product> findAllByFilters(List<String> categoryPaths,
                                   ProductGenderType gender,
                                   String keyword, // TODO: 검색 확장시 분리.
                                   Long brandId);
}
