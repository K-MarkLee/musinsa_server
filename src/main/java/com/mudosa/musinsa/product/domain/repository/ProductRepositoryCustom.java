package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.vo.ProductGenderType;

import java.util.List;
import java.util.Optional;

// 상품 상세 조회와 검색을 위한 커스텀 리포지토리 인터페이스이다.
public interface ProductRepositoryCustom {

    // 상세 화면에 필요한 연관 엔티티를 함께 로딩해 반환한다.
    Optional<Product> findDetailById(Long productId);

    // 조건값을 기반으로 상품 목록을 필터링해 반환한다.
    List<Product> findAllByFilters(List<String> categoryPaths,
                                   ProductGenderType.Type gender,
                                   String keyword,
                                   Long brandId);
}
