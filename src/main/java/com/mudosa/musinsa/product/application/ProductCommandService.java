package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.application.mapper.ProductCommandMapper;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductLike;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductLikeRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 상품 생성/수정/옵션 관리 등 상태 변화를 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCommandService {

	private final ProductRepository productRepository;
	private final OptionValueRepository optionValueRepository;
	private final ProductLikeRepository productLikeRepository;

	/**
	 * 커맨드 객체를 받아 상품과 하위 옵션을 생성한다.
	 */
	@Transactional
	public Long createProduct(ProductCreateRequest request,
							  Brand brand,
							  Category category) {
		if (request.getImages() == null || request.getImages().isEmpty()) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상품 이미지는 최소 1장 이상 등록해야 합니다.");
		}
		if (request.getOptions() == null || request.getOptions().isEmpty()) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상품 옵션은 최소 1개 이상 등록해야 합니다.");
		}

		ProductGenderType genderType = parseGenderType(request.getProductGenderType());
		Map<Long, OptionValue> optionValueMap = loadOptionValues(request.getOptions());

		List<Product.ImageRegistration> imageRegistrations = ProductCommandMapper.toImageRegistrations(request.getImages());

		String brandName = brand != null ? brand.getNameKo() : request.getBrandName();
		String categoryPath = category != null ? category.buildPath() : request.getCategoryPath();

		Product product = Product.builder()
			.brand(brand)
			.productName(request.getProductName())
			.productInfo(request.getProductInfo())
			.productGenderType(genderType)
			.brandName(brandName)
			.categoryPath(categoryPath)
			.isAvailable(request.getIsAvailable())
			.build();

		product.registerImages(imageRegistrations);

		request.getOptions().forEach(option -> {
			Inventory inventory = Inventory.builder()
				.stockQuantity(new StockQuantity(option.getStockQuantity()))
				.build();

			ProductOption productOption = ProductOption.builder()
				.product(product)
				.productPrice(new Money(option.getProductPrice()))
				.inventory(inventory)
				.build();

			option.getOptionValueIds().forEach(optionValueId -> {
				OptionValue optionValue = optionValueMap.get(optionValueId);
				productOption.addOptionValue(ProductOptionValue.builder()
					.productOption(productOption)
					.optionValue(optionValue)
					.build());
			});

			product.addProductOption(productOption);
		});

		Product saved = productRepository.save(product);
		return saved.getProductId();
	}

	/**
	 * 상품 기본 정보를 갱신한다.
	 */
	@Transactional
	public ProductDetailResponse updateProduct(Long brandId,
											   Long productId,
											   ProductUpdateRequest request) {
		Product product = productRepository.findDetailById(productId)
			.orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
		validateBrandOwnership(product, brandId);
		return applyUpdates(product, request);
	}

	/**
	 * 상품 옵션을 추가하고 결과 상세 정보를 반환한다.
	 */
	@Transactional
	public ProductDetailResponse.OptionDetail addProductOption(Long brandId,
															   Long productId,
															   ProductOptionCreateRequest request) {
		Product product = productRepository.findDetailById(productId)
			.orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
		validateBrandOwnership(product, brandId);

		if (request == null) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "옵션 정보가 필요합니다.");
		}
		if (request.getProductPrice() == null || request.getStockQuantity() == null) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "옵션 가격과 재고는 필수입니다.");
		}
		if (request.getOptionValueIds() == null || request.getOptionValueIds().isEmpty()) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "옵션 값 ID는 최소 1개 이상이어야 합니다.");
		}

		Map<Long, OptionValue> optionValueMap = loadOptionValuesByIds(request.getOptionValueIds());

		Inventory inventory = Inventory.builder()
			.stockQuantity(new StockQuantity(request.getStockQuantity()))
			.build();

		ProductOption productOption = ProductOption.builder()
			.product(product)
			.productPrice(new Money(request.getProductPrice()))
			.inventory(inventory)
			.build();

		request.getOptionValueIds().forEach(optionValueId -> {
			OptionValue optionValue = optionValueMap.get(optionValueId);
			productOption.addOptionValue(ProductOptionValue.builder()
				.productOption(productOption)
				.optionValue(optionValue)
				.build());
		});

	product.addProductOption(productOption);
	productRepository.flush(); 
	return ProductCommandMapper.toOptionDetail(productOption);
	}

	/**
	 * 상품 옵션을 제거한다.
	 */
	@Transactional
	public void removeProductOption(Long brandId,
									Long productId,
									Long productOptionId) {
		Product product = productRepository.findDetailById(productId)
			.orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
		validateBrandOwnership(product, brandId);

		ProductOption targetOption = product.getProductOptions().stream()
			.filter(option -> Objects.equals(option.getProductOptionId(), productOptionId))
			.findFirst()
			.orElseThrow(() -> new EntityNotFoundException("Product option not found: " + productOptionId));

		product.removeProductOption(targetOption);
	}

	/**
	 * 특정 사용자의 좋아요 상태를 토글한 뒤 결과 카운트를 반환한다. 현재는 미사용
	 */
	@Transactional
	public long toggleLike(Long productId, Long userId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

		productLikeRepository.findByProductAndUserId(product, userId)
			.ifPresentOrElse(
				productLikeRepository::delete,
				() -> productLikeRepository.save(ProductLike.builder()
					.product(product)
					.userId(userId)
					.build())
			);

		return productLikeRepository.countByProduct(product);
	}

	// 업데이트 요청을 상품에 적용하고 상세 응답을 생성한다.
	private ProductDetailResponse applyUpdates(Product product,
											   ProductUpdateRequest request) {
		if (!request.hasUpdatableField()) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "변경할 데이터가 없습니다.");
		}

		boolean changed = product.updateBasicInfo(
			request.getProductName(),
			request.getProductInfo(),
			null
		);

		if (request.getIsAvailable() != null) {
			if (!Objects.equals(product.getIsAvailable(), request.getIsAvailable())) {
				product.changeAvailability(request.getIsAvailable());
				changed = true;
			}
		}

		if (request.getImages() != null) {
			if (request.getImages().isEmpty()) {
				throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상품 이미지는 최소 1장 이상 등록해야 합니다.");
			}
			List<Product.ImageRegistration> registrations = request.getImages().stream()
				.map(image -> new Product.ImageRegistration(
					image.getImageUrl(),
					Boolean.TRUE.equals(image.getIsThumbnail())
				))
				.collect(Collectors.toList());
			product.registerImages(registrations);
			changed = true;
		}

		if (!changed) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "변경된 항목이 없습니다.");
		}

	return ProductCommandMapper.toProductDetail(product);
	}

	// 브랜드 소유권을 검증한다.
	private void validateBrandOwnership(Product product, Long brandId) {
		if (brandId == null) {
			return;
		}
		if (product.getBrand() == null
			|| product.getBrand().getBrandId() == null
			|| !Objects.equals(product.getBrand().getBrandId(), brandId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "해당 브랜드의 상품이 아닙니다.");
		}
	}

	// 옵션 값 ID 목록에 해당하는 옵션 값 엔티티들을 로드한다.
	private Map<Long, OptionValue> loadOptionValues(List<ProductCreateRequest.OptionCreateRequest> optionSpecs) {
		if (optionSpecs == null || optionSpecs.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<Long> optionValueIds = optionSpecs.stream()
			.filter(Objects::nonNull)
			.flatMap(spec -> spec.getOptionValueIds().stream())
			.collect(Collectors.toSet());

		if (optionValueIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return loadOptionValuesByIds(new ArrayList<>(optionValueIds));
	}

	private Map<Long, OptionValue> loadOptionValuesByIds(List<Long> optionValueIds) {
		if (optionValueIds == null || optionValueIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<Long> uniqueOptionValueIds = new HashSet<>(optionValueIds);
		if (uniqueOptionValueIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Long, OptionValue> optionValueMap = optionValueRepository.findAllByOptionValueIdIn(new ArrayList<>(uniqueOptionValueIds))
			.stream()
			.collect(Collectors.toMap(OptionValue::getOptionValueId, Function.identity()));

		if (optionValueMap.size() != uniqueOptionValueIds.size()) {
			Set<Long> missingIds = new HashSet<>(uniqueOptionValueIds);
			missingIds.removeAll(optionValueMap.keySet());
			throw new BusinessException(ErrorCode.VALIDATION_ERROR,
				"존재하지 않는 옵션 값 ID가 포함되어 있습니다: " + missingIds);
		}

		return optionValueMap;
	}

	private ProductGenderType parseGenderType(String gender) {
		try {
			return ProductGenderType.valueOf(gender.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException | NullPointerException ex) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 상품 성별 타입입니다.");
		}
	}

}
