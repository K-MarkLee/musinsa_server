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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

		// 2. 조건 기반 상품 조회 (List 로 반환되어 별도 정렬/페이징 필요)
		List<Product> products = new ArrayList<>(productRepository.findAllByFilters(categoryPaths, gender, keyword, brandId));

		// 3. 가격 정렬 옵션 적용
		if (priceSort != null) {
			Comparator<Product> comparator = Comparator.comparing(ProductQueryMapper::calculateLowestPrice);
			if (priceSort == ProductSearchCondition.PriceSort.HIGHEST) {
				comparator = comparator.reversed();
			}
			products.sort(comparator);
		}

		// 4. 수동 페이징
		Page<Product> page = toPage(products, pageable);

		// 5. 응답 DTO 변환
		List<ProductSearchResponse.ProductSummary> summaries = page.getContent().stream()
			.map(ProductQueryMapper::toProductSummary)
			.collect(Collectors.toList());

		int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
		int pageSize = pageable.isPaged() ? pageable.getPageSize() : summaries.size();
		int totalPages = pageSize > 0
			? (int) Math.ceil((double) page.getTotalElements() / Math.max(pageSize, 1))
			: (page.getTotalElements() > 0 ? 1 : 0);

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
			.orElseThrow(() -> new EntityNotFoundException("해당 상품을 찾을 수 없습니다: " + productId));

	// 2. 응답 DTO 변환
	return ProductQueryMapper.toProductDetail(product);
	}

	/**
	 * 정렬된 도메인 목록을 입력 Pageable 에 맞게 슬라이스한다.
	 */
	private Page<Product> toPage(List<Product> products, Pageable pageable) {
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(products, Pageable.unpaged(), products.size());
		}

		// 정렬 결과를 유지한 채 서브리스트로 잘라 Spring Page 형태로 감싼다.
		int total = products.size();
		int fromIndex = Math.min((int) pageable.getOffset(), total);
		int toIndex = Math.min(fromIndex + pageable.getPageSize(), total);
		List<Product> content = new ArrayList<>(products.subList(fromIndex, toIndex));
		return new PageImpl<>(content, pageable, total);
	}
}
