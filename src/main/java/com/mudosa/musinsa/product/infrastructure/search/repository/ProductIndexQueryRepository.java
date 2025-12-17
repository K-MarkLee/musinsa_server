package com.mudosa.musinsa.product.infrastructure.search.repository;

import com.mudosa.musinsa.product.infrastructure.search.dto.ProductIndexDto;

import java.util.List;

/**
 * ES 색인용으로 옵션 단위 데이터를 조회하는 전용 리포지토리.
 * 대량 색인을 위해 projection(필드 최소화) + 커서 기반 페이징을 사용한다.
 */
public interface ProductIndexQueryRepository {

    /**
     * 상품 옵션을 커서 기반으로 chunk 조회한다.
     *
     * @param lastOptionId 마지막으로 읽은 옵션 ID (없으면 null)
     * @param pageSize     한번에 읽을 개수
     * @return ProductOptionIndexDto 목록
     */
    List<ProductIndexDto> findChunk(Long lastOptionId, int pageSize);
}
