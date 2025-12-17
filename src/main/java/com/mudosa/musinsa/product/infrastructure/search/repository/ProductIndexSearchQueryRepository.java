package com.mudosa.musinsa.product.infrastructure.search.repository;

import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;

import java.util.List;

/**
 * 상품 검색/필터용 ES 쿼리 리포지토리.
 * 기존 네이밍을 유지해 검색/필터 전용 메서드를 제공한다.
 */
public interface ProductIndexSearchQueryRepository {

    SearchResult searchByKeywordWithFilters(ProductSearchCondition condition, List<String> tokens, int page);

    SearchResult findAllByFiltersWithCursor(ProductSearchCondition condition);

    record SearchResult(List<ProductSearchResponse.ProductSummary> products,
                        boolean hasNext,
                        Long totalCount) {
    }
}
