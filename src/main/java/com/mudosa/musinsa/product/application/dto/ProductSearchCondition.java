package com.mudosa.musinsa.product.application.dto;

import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

/**
 * 상품 검색 파라미터를 서비스 계층에서 사용할 조건 객체로 변환한다.
 */
@Getter
@Builder
public class ProductSearchCondition {
	private final String keyword;
	private final List<String> categoryPaths;
	private final ProductGenderType gender;
	private final Long brandId;
	private final Pageable pageable;
	private final PriceSort priceSort;

	public Pageable getPageable() {
		return pageable != null ? pageable : Pageable.unpaged();
	}

	public List<String> getCategoryPaths() {
		return categoryPaths != null ? categoryPaths : Collections.emptyList();
	}

	public enum PriceSort {
		LOWEST,
		HIGHEST
	}
}