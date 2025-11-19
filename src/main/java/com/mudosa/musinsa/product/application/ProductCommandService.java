package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductManagerResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.application.mapper.ProductCommandMapper;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.model.Image;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductLike;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductLikeRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 관리자용 상품 관리 및 조회 등을 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class ProductCommandService {
	
	private final ProductRepository productRepository;
	private final OptionValueRepository optionValueRepository;
	private final ProductLikeRepository productLikeRepository;
	private final BrandMemberRepository brandMemberRepository;

	/**
	 * 커맨드 객체를 받아 상품과 하위 옵션을 생성한다.
	 */
	@Transactional
	public Long createProduct(ProductCreateRequest request,
						  Brand brand,
						  Category category,
						  Long currentUserId) {
		validateBrandMember(brand.getBrandId(), currentUserId);
		ProductGenderType genderType = request.getProductGenderType();
		
		// 옵션 값 ID 목록 추출
		Set<Long> optionValueIds = request.getOptions().stream()
			.filter(Objects::nonNull)
			.flatMap(spec -> spec.getOptionValueIds().stream())
			.collect(Collectors.toSet());
		
		Map<Long, OptionValue> optionValueMap = loadOptionValuesByIds(new ArrayList<>(optionValueIds));
		
		// 상품 생성 기본 정보 설정
		Product product = Product.builder()
			.brand(brand)
			.productName(request.getProductName())
			.productInfo(request.getProductInfo())
			.productGenderType(genderType)
			.brandName(brand.getNameKo())
			.categoryPath(category.buildPath())
			.isAvailable(request.getIsAvailable())
			.build();

		// 이미지 생성 및 추가
		addImagesToProduct(product, request.getImages());

		// 상품 옵션을 직접 생성해서 상품에 추가
		request.getOptions().forEach(option -> {
			Money price = option.getProductPrice() != null ? new Money(option.getProductPrice()) : null;
			ProductOption productOption = createProductOption(
				product,
				price,
				option.getStockQuantity(),
				option.getOptionValueIds(),
				optionValueMap
			);
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
											   ProductUpdateRequest request,
											   Long currentUserId) {
		validateBrandMember(brandId, currentUserId);
		Product product = productRepository.findDetailByIdForManager(productId, brandId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. productId=" + productId));
		validateBrandOwnership(product, brandId);
		return applyUpdates(product, request);
	}

	/**
	 * 상품 옵션을 추가하고 결과 상세 정보를 반환한다.
	 */
	@Transactional
	public ProductDetailResponse.OptionDetail addProductOption(Long brandId,
												       Long productId,
												       ProductOptionCreateRequest request,
												       Long currentUserId) {
		validateBrandMember(brandId, currentUserId);
		Product product = productRepository.findDetailByIdForManager(productId, brandId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. productId=" + productId));
		validateBrandOwnership(product, brandId);		
		if (request == null) {
			throw new BusinessException(ErrorCode.PRODUCT_OPTION_REQUIRED, "옵션 정보가 필요합니다.");
		}
		if (request.getProductPrice() == null || request.getStockQuantity() == null) {
			throw new BusinessException(ErrorCode.PRODUCT_OPTION_REQUIRED, "옵션 가격과 재고는 필수입니다.");
		}
		if (request.getOptionValueIds() == null || request.getOptionValueIds().isEmpty()) {
			throw new BusinessException(ErrorCode.PRODUCT_OPTION_REQUIRED, "옵션 값 ID는 최소 1개 이상이어야 합니다.");
		}

		Map<Long, OptionValue> optionValueMap = loadOptionValuesByIds(request.getOptionValueIds());

		Money optionPrice = request.getProductPrice() != null ? new Money(request.getProductPrice()) : null;
		ProductOption productOption = createProductOption(
			product,
			optionPrice,
			request.getStockQuantity(),
			request.getOptionValueIds(),
			optionValueMap
		);
		
		product.addProductOption(productOption);
		productRepository.flush(); 
		return ProductCommandMapper.toOptionDetail(productOption);
	}
	
	/**
	 * 브랜드 매니저용: 해당 브랜드의 모든 상품 목록을 조회한다 (isAvailable=false 포함)
	 */
	@Transactional(readOnly = true)
	public List<ProductManagerResponse> getBrandProductsForManager(Long brandId, Long currentUserId) {
		validateBrandMember(brandId, currentUserId);
		
		List<Product> products = productRepository.findAllByBrandForManager(brandId);
		
		return products.stream()
			.map(this::toManagerResponse)
			.collect(Collectors.toList());
	}

	/**
	 * 브랜드 매니저용: 특정 상품 상세 정보를 조회한다 (isAvailable=false 포함)
	 */
	@Transactional(readOnly = true)
	public ProductManagerResponse getProductDetailForManager(Long brandId, Long productId, Long currentUserId) {
		validateBrandMember(brandId, currentUserId);
		
		Product product = productRepository.findDetailByIdForManager(productId, brandId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, 
				"상품을 찾을 수 없습니다. productId=" + productId + ", brandId=" + brandId));
		
		return toManagerResponse(product);
	}

	/**
	 * 특정 사용자의 좋아요 상태를 토글한 뒤 결과 카운트를 반환한다. 현재는 미사용
	 */
	@Transactional
	public long toggleLike(Long productId, Long userId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. productId=" + productId));

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
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상품 이름, 설명, 판매가능여부, 이미지만 수정이 가능합니다.");
		}

		boolean changed = product.updateBasicInfo(
			request.getProductName(),
			request.getProductInfo()
		);

		if (request.getIsAvailable() != null) {
			product.changeAvailability(request.getIsAvailable());
			changed = true;
		}

		if (request.getImages() != null) {
			if (request.getImages().isEmpty()) {
				throw new BusinessException(ErrorCode.IMAGE_REQUIRED, "상품 이미지는 최소 1장 이상 등록해야 합니다.");
			}
			// 기존 이미지 모두 제거
			product.getImages().clear();
			// 새로운 이미지 직접 생성하여 추가
			request.getImages().forEach(image -> {
				Image img = Image.create(image.getImageUrl(), Boolean.TRUE.equals(image.getIsThumbnail()));
				product.addImage(img);
			});
			changed = true;
		}

		if (!changed) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "변경된 항목이 없습니다.");
		}

		return ProductCommandMapper.toProductDetail(product);
	}

	// 브랜드 멤버 권한을 검증한다.
	private void validateBrandMember(Long brandId, Long userId) {
		if (!brandMemberRepository.existsByBrand_BrandIdAndUserId(brandId, userId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN, "브랜드 멤버 권한이 없습니다. brandId=" + brandId + ", userId=" + userId);
		}
	}

	// 브랜드 소유권을 검증한다.
	private void validateBrandOwnership(Product product, Long brandId) {
		if (brandId == null) {
			return;
		}
		if (product.getBrand() == null
			|| product.getBrand().getBrandId() == null
			|| !Objects.equals(product.getBrand().getBrandId(), brandId)) {
			throw new BusinessException(ErrorCode.NOT_BRAND_PRODUCT);
		}
	}

	// 옵션 값 ID 목록에 해당하는 옵션 값 엔티티들을 로드한다.
	private Map<Long, OptionValue> loadOptionValuesByIds(List<Long> optionValueIds) {
		if (optionValueIds == null || optionValueIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<Long> uniqueOptionValueIds = new HashSet<>(optionValueIds);

		Map<Long, OptionValue> optionValueMap = optionValueRepository.findAllByOptionValueIdIn(new ArrayList<>(uniqueOptionValueIds))
			.stream()
			.collect(Collectors.toMap(OptionValue::getOptionValueId, Function.identity()));

		if (optionValueMap.size() != uniqueOptionValueIds.size()) {
			Set<Long> missingIds = new HashSet<>(uniqueOptionValueIds);
			missingIds.removeAll(optionValueMap.keySet());
			throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE, "존재하지 않는 옵션 값 ID가 포함되어 있습니다: " + missingIds);
		}

		return optionValueMap;
	}

	// 상품 옵션을 생성하는 공통 메서드
	private ProductOption createProductOption(Product product, 
											  Money productPrice, 
											  Integer stockQuantity, 
											  List<Long> optionValueIds,
											  Map<Long, OptionValue> optionValueMap) {
		// 1. Inventory 생성
		Inventory inventory = Inventory.builder()
			.stockQuantity(new StockQuantity(stockQuantity))
			.build();

		// 2. OptionValue 엔티티 매핑
		List<OptionValue> optionValues = optionValueIds.stream()
			.map(optionValueMap::get)
			.collect(Collectors.toList());

		// 3. ProductOption 생성
		ProductOption productOption = ProductOption.create(product, productPrice, inventory);
		
		// 4. ProductOptionValue 생성 및 추가
		optionValues.forEach(optionValue -> {
			ProductOptionValue productOptionValue = ProductOptionValue.create(productOption, optionValue);
			productOption.addOptionValue(productOptionValue);
		});
		
		return productOption;
	}

	// 상품에 이미지를 추가하는 헬퍼 메서드
	private void addImagesToProduct(Product product, List<ProductCreateRequest.ImageCreateRequest> images) {
		images.forEach(image -> {
			Image img = Image.create(image.getImageUrl(), image.getIsThumbnail());
			product.addImage(img);
		});
	}

	// Product를 ProductManagerResponse로 변환하는 헬퍼 메서드
	private ProductManagerResponse toManagerResponse(Product product) {
		List<ProductManagerResponse.ImageInfo> imageInfos = product.getImages().stream()
			.map(image -> ProductManagerResponse.ImageInfo.builder()
				.imageId(image.getImageId())
				.imageUrl(image.getImageUrl())
				.isThumbnail(image.getIsThumbnail())
				.build())
			.collect(Collectors.toList());

		List<ProductManagerResponse.OptionInfo> optionInfos = product.getProductOptions().stream()
			.map(option -> ProductManagerResponse.OptionInfo.builder()
				.optionId(option.getProductOptionId())
				.price(option.getProductPrice() != null ? option.getProductPrice().getAmount() : null)
				.stockQuantity(option.getInventory().getStockQuantity().getValue())
				.optionValues(option.getProductOptionValues().stream()
					.map(pov -> pov.getOptionValue().getOptionValue()) // optionValue 필드 사용
					.collect(Collectors.toList()))
				.build())
			.collect(Collectors.toList());

		return ProductManagerResponse.builder()
			.productId(product.getProductId())
			.productName(product.getProductName())
			.productInfo(product.getProductInfo())
			.isAvailable(product.getIsAvailable())
			.brandName(product.getBrandName())
			.categoryPath(product.getCategoryPath())
			.productGenderType(product.getProductGenderType())
			.createdAt(product.getCreatedAt())
			.updatedAt(product.getUpdatedAt())
			.images(imageInfos)
			.options(optionInfos)
			.build();
	}

}
