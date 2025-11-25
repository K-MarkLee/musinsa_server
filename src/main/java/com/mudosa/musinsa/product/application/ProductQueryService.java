package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.application.mapper.ProductQueryMapper;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 조회 전용 서비스. 목록, 상세, 검색 응답을 구성한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

	private final ProductRepository productRepository;

	/**
	 * 검색 조건에 맞는 상품을 조회해 페이지 형태로 반환한다.
	 */
	public ProductSearchResponse searchProducts(ProductSearchCondition condition) {
		// 1. 검색 조건 파싱
		Pageable pageable = condition != null ? condition.getPageable() : Pageable.unpaged();
		ProductGenderType gender = condition != null ? condition.getGender() : null;
		String keyword = condition != null ? condition.getKeyword() : null;
		Long brandId = condition != null ? condition.getBrandId() : null;
		ProductSearchCondition.PriceSort priceSort = condition != null ? condition.getPriceSort() : null;
		List<String> categoryPaths = condition != null ? condition.getCategoryPaths() : Collections.emptyList();

		// 2. 키워드 유무에 따라 적절한 검색 메서드 호출
		Page<Product> page;
		if (keyword != null && !keyword.isBlank()) {
			// 키워드 검색 + 필터링
			page = productRepository.searchByKeywordWithFilters(
				keyword, categoryPaths, gender, brandId, priceSort, pageable);
		} else {
			// 순수 필터링
			page = productRepository.findAllByFiltersWithPagination(
				categoryPaths, gender, brandId, priceSort, pageable);
		}

		// 3. 응답 DTO 변환
		List<ProductSearchResponse.ProductSummary> summaries = page.getContent().stream()
			.map(ProductQueryMapper::toProductSummary)
			.collect(Collectors.toList());

		int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
		int pageSize = pageable.isPaged() ? pageable.getPageSize() : summaries.size();
		int totalPages = page.getTotalPages();

		return ProductSearchResponse.builder()
		        .products(summaries)
		        .totalElements(page.getTotalElements())
		        .totalPages(totalPages)
		        .page(pageNumber)
		        .size(pageSize)
		        .build();
	}

	/**
	 * 단일 상품 상세 정보를 조회한다.
	 */
	public ProductDetailResponse getProductDetail(Long productId) {
		// 1. 상품 엔티티 조회 (상품 + 옵션)
		Product product = productRepository.findDetailById(productId)
			.orElseThrow(() -> new EntityNotFoundException("해당 상품을 찾을 수 없거나 비활성화된 상품입니다: " + productId +"번 상품"));

		// 2. 응답 DTO 변환
		return ProductQueryMapper.toProductDetail(product);
	}
}
