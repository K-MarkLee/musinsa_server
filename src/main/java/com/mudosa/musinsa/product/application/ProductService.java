package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.OptionName;
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
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// 상품 시나리오 전반을 담당하는 단일 서비스 계층이며 DTO 이전 단계에서 도메인 로직을 묶어 제공한다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductLikeRepository productLikeRepository;

    // ProductCreateCommand를 기반으로 상품과 연관 엔티티를 한 번에 저장한다.
    @Transactional
    public Long createProduct(ProductCreateCommand command) {
        Product product = Product.builder()
            .brand(command.getBrand())
            .productName(command.getProductName())
            .productInfo(command.getProductInfo())
            .productGenderType(command.getProductGenderType())
            .brandName(command.getBrandName())
            .categoryPath(command.getCategoryPath())
            .isAvailable(command.getIsAvailable())
            .build();

        List<Product.ImageRegistration> imageRegistrations = command.getImages().stream()
            .map(imageSpec -> new Product.ImageRegistration(imageSpec.imageUrl(), imageSpec.isThumbnail()))
            .collect(Collectors.toList());
        product.registerImages(imageRegistrations);

        Map<Long, OptionValue> optionValueMap = loadOptionValues(command.getOptions());

        command.getOptions().forEach(optionSpec -> {
            Inventory inventory = Inventory.builder()
                .stockQuantity(new StockQuantity(optionSpec.stockQuantity()))
                .build();

            ProductOption productOption = ProductOption.builder()
                .product(product)
                .productPrice(new Money(optionSpec.productPrice()))
                .inventory(inventory)
                .build();

                List<Long> optionValueIds = optionSpec.optionValueIds() != null
                    ? optionSpec.optionValueIds()
                    : Collections.emptyList();

                optionValueIds.stream()
                    .map(id -> ProductOptionValue.builder()
                        .productOption(productOption)
                        .optionValue(optionValueMap.get(id))
                        .build())
                    .forEach(productOption::addOptionValue);

            product.addProductOption(productOption);
        });

        Product saved = productRepository.save(product);
        return saved.getProductId();
    }

    // 외부에서 받은 DTO와 선행 조회된 도메인 객체를 조합해 ProductCreateCommand를 생성하고 저장한다.
    @Transactional
    public Long createProduct(ProductCreateRequest request,
                              Brand brand,
                              Category category) {
    ProductGenderType genderType = parseGenderType(request.getProductGenderType());

        validateDenormalizedFields(request, brand, category);

        List<ProductCreateCommand.ImageSpec> imageSpecs = request.getImages().stream()
            .map(image -> new ProductCreateCommand.ImageSpec(
                image.getImageUrl(),
                Boolean.TRUE.equals(image.getIsThumbnail())))
            .collect(Collectors.toList());

        List<ProductCreateCommand.OptionSpec> optionSpecs = request.getOptions().stream()
            .map(option -> new ProductCreateCommand.OptionSpec(
                option.getProductPrice(),
                option.getStockQuantity(),
                option.getOptionValueIds()))
            .collect(Collectors.toList());

        ProductCreateCommand command = ProductCreateCommand.builder()
            .brand(brand)
            .productName(request.getProductName())
            .productInfo(request.getProductInfo())
            .productGenderType(genderType)
            .brandName(request.getBrandName())
            .categoryPath(request.getCategoryPath())
            .isAvailable(request.getIsAvailable())
            .images(imageSpecs)
            .options(optionSpecs)
            .build();

        return createProduct(command);
    }

    // 역정규화된 필드가 실제 도메인 객체와 일치하는지 검증해 데이터 불일치를 방지한다.
    private void validateDenormalizedFields(ProductCreateRequest request,
                                            Brand brand,
                                            Category category) {
        if (!brand.getNameKo().equals(request.getBrandName())) {
            throw new IllegalArgumentException("브랜드 정보가 일치하지 않습니다. brandId=" + brand.getBrandId());
        }

        if (category == null || !category.buildPath().equals(request.getCategoryPath())) {
            throw new IllegalArgumentException("카테고리 경로가 일치하지 않습니다. categoryPath=" + request.getCategoryPath());
        }
    }

    // 옵션 등록 시 필요한 OptionValue를 한 번에 적재하고 누락된 ID를 사전에 검출한다.
    private Map<Long, OptionValue> loadOptionValues(List<ProductCreateCommand.OptionSpec> optionSpecs) {
        Set<Long> optionValueIds = optionSpecs.stream()
                .filter(spec -> spec.optionValueIds() != null)
                .flatMap(spec -> spec.optionValueIds().stream())
            .collect(Collectors.toSet());

        if (optionValueIds.isEmpty()) {
            return Collections.emptyMap();
        }

            Map<Long, OptionValue> optionValueMap = optionValueRepository.findAllByOptionValueIdIn(new ArrayList<>(optionValueIds))
            .stream()
                .collect(Collectors.toMap(OptionValue::getOptionValueId, Function.identity()));

            if (optionValueMap.size() != optionValueIds.size()) {
                Set<Long> missingIds = new HashSet<>(optionValueIds);
                missingIds.removeAll(optionValueMap.keySet());
                throw new IllegalArgumentException("존재하지 않는 옵션 값 ID가 포함되어 있습니다: " + missingIds);
            }

            return optionValueMap;
    }

    // 상세 화면에 필요한 연관 정보를 로딩하고 응답 DTO로 변환한다.
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        long likeCount = productLikeRepository.countByProduct(product);
        return mapToDetailResponse(product, likeCount);
    }

    // 로딩된 상품 엔티티를 상세 응답 DTO 구조로 풀어낸다.
    private ProductDetailResponse mapToDetailResponse(Product product, long likeCount) {
        List<ProductDetailResponse.ImageResponse> imageResponses = product.getImages().stream()
            .map(image -> ProductDetailResponse.ImageResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .isThumbnail(Boolean.TRUE.equals(image.getIsThumbnail()))
                .build())
            .collect(Collectors.toList());

        List<ProductDetailResponse.OptionDetail> optionDetails = product.getProductOptions().stream()
            .map(option -> {
                List<ProductDetailResponse.OptionDetail.OptionValueDetail> optionValueDetails = option.getProductOptionValues().stream()
                    .map(mapping -> {
                        OptionValue optionValue = mapping.getOptionValue();
                        OptionName optionName = optionValue != null ? optionValue.getOptionName() : null;
                        return ProductDetailResponse.OptionDetail.OptionValueDetail.builder()
                            .optionValueId(optionValue != null ? optionValue.getOptionValueId() : null)
                            .optionNameId(optionName != null ? optionName.getOptionNameId() : null)
                            .optionName(optionName != null ? optionName.getOptionName() : null)
                            .optionValue(optionValue != null ? optionValue.getOptionValue() : null)
                            .build();
                    })
                    .collect(Collectors.toList());

                Integer stockQuantity = null;
                Boolean hasStock = null;
                if (option.getInventory() != null && option.getInventory().getStockQuantity() != null) {
                    stockQuantity = option.getInventory().getStockQuantity().getValue();
                    hasStock = stockQuantity > 0;
                }

                return ProductDetailResponse.OptionDetail.builder()
                    .optionId(option.getProductOptionId())
                    .productPrice(option.getProductPrice() != null ? option.getProductPrice().getAmount() : null)
                    .stockQuantity(stockQuantity)
                    .hasStock(hasStock)
                    .optionValues(optionValueDetails)
                    .build();
            })
            .collect(Collectors.toList());

        return ProductDetailResponse.builder()
            .productId(product.getProductId())
            .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
            .brandName(product.getBrandName())
            .productName(product.getProductName())
            .productInfo(product.getProductInfo())
            .productGenderType(product.getProductGenderType() != null
                ? product.getProductGenderType().name()
                : null)
            .isAvailable(product.getIsAvailable())
            .categoryPath(product.getCategoryPath())
            .likeCount(likeCount)
            .images(imageResponses)
            .options(optionDetails)
            .build();
    }

    // 검색 조건을 기준으로 상품 목록을 조회하고 필요 시 가격 정렬 및 페이징을 수행한다.
    public ProductSearchResponse searchProducts(ProductSearchCondition condition) {
        Pageable pageable = condition != null ? condition.getPageable() : Pageable.unpaged();

        ProductGenderType gender = condition != null ? condition.getGender() : null;
        String keyword = condition != null ? condition.getKeyword() : null;
        Long brandId = condition != null ? condition.getBrandId() : null;
        ProductSearchCondition.PriceSort priceSort = condition != null ? condition.getPriceSort() : null;

        List<String> categoryPaths = condition != null ? condition.getCategoryPaths() : Collections.emptyList();
        List<Product> products = new ArrayList<>(productRepository.findAllByFilters(categoryPaths, gender, keyword, brandId));

        if (priceSort != null) {
            Comparator<Product> comparator = Comparator.comparing(this::extractLowestPrice);
            if (priceSort == ProductSearchCondition.PriceSort.HIGHEST) {
                comparator = comparator.reversed();
            }
            products.sort(comparator);
        }

        Page<Product> page = toPage(products, pageable);

        List<ProductSearchResponse.ProductSummary> summaries = page.getContent().stream()
            .map(this::mapToProductSummary)
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

    // 특정 사용자의 좋아요 상태를 토글하고 결과 카운트를 반환한다.
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

    // 상품 기본 정보를 갱신한다.
    @Transactional
    public ProductDetailResponse updateProduct(Long productId,
                                               ProductUpdateRequest request) {
        Product product = productRepository.findDetailById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        ProductGenderType genderType = parseGenderType(request.getProductGenderType());

        String brandName = request.getBrandName() != null
            ? request.getBrandName()
            : product.getBrandName();

        product.updateBasicInfo(
            request.getProductName(),
            request.getProductInfo(),
            genderType,
            brandName
        );

        if (request.getIsAvailable() != null) {
            product.changeAvailability(request.getIsAvailable());
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<Product.ImageRegistration> registrations = request.getImages().stream()
                .map(image -> new Product.ImageRegistration(
                    image.getImageUrl(),
                    Boolean.TRUE.equals(image.getIsThumbnail())
                ))
                .collect(Collectors.toList());
            product.registerImages(registrations);
        }

        // TODO 옵션 가격/재고 수정은 정책 확정 후 구현한다.

        long likeCount = productLikeRepository.countByProduct(product);
        return mapToDetailResponse(product, likeCount);
    }

    // 상품을 비활성화해 소프트 삭제 상태로 전환한다.
    @Transactional
    public void disableProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        product.changeAvailability(false);
    }

    // 서비스 내부에서 상품 생성 파라미터를 묶어 전달하기 위한 커맨드 객체이다.
    @Getter
    public static class ProductCreateCommand {
        private final Brand brand;
        private final String productName;
        private final String productInfo;
        private final ProductGenderType productGenderType;
        private final String brandName;
        private final String categoryPath;
        private final Boolean isAvailable;
        private final List<ImageSpec> images;
        private final List<OptionSpec> options;

        @Builder
        public ProductCreateCommand(Brand brand,
                                    String productName,
                                    String productInfo,
                                    ProductGenderType productGenderType,
                                    String brandName,
                                    String categoryPath,
                                    Boolean isAvailable,
                                    List<ImageSpec> images,
                                    List<OptionSpec> options) {
            this.brand = brand;
            this.productName = productName;
            this.productInfo = productInfo;
            this.productGenderType = productGenderType;
            this.brandName = brandName;
            this.categoryPath = categoryPath;
            this.isAvailable = isAvailable;
            this.images = images != null ? images : Collections.emptyList();
            this.options = options != null ? options : Collections.emptyList();
        }

        public record ImageSpec(String imageUrl, boolean isThumbnail) {}

        public record OptionSpec(BigDecimal productPrice,
                                 int stockQuantity,
                                 List<Long> optionValueIds) {}
    }

    // 검색 조건을 전달하기 위한 내부 DTO로 페이지 정보와 정렬 기준을 포함한다.
    @Getter
    @Builder
    public static class ProductSearchCondition {
        private final String keyword;
    private final List<String> categoryPaths;
    private final ProductGenderType gender;
        private final Long brandId;
        private final Pageable pageable;
        private final PriceSort priceSort;

        // pageable이 null일 경우 기본값을 반환한다.
        public Pageable getPageable() {
            return pageable != null ? pageable : Pageable.unpaged();
        }

        // 카테고리 경로 리스트를 널 안전하게 반환한다.
        public List<String> getCategoryPaths() {
            return categoryPaths != null ? categoryPaths : Collections.emptyList();
        }

        public enum PriceSort {
            LOWEST,
            HIGHEST
        }
    }

    // 전달받은 카테고리 ID 목록을 경로 문자열 집합으로 변환한다.
    // 상품 옵션 중 최저 가격을 계산해 정렬 및 요약 정보에 활용한다.
    private BigDecimal extractLowestPrice(Product product) {
        return product.getProductOptions().stream()
            .map(ProductOption::getProductPrice)
            .filter(Objects::nonNull)
            .map(Money::getAmount)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    // 조회된 상품 리스트를 Pageable에 맞게 슬라이스해 Page 형태로 반환한다.
    private Page<Product> toPage(List<Product> products, Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(products, Pageable.unpaged(), products.size());
        }

        int total = products.size();
        int fromIndex = Math.min((int) pageable.getOffset(), total);
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), total);
        List<Product> content = new ArrayList<>(products.subList(fromIndex, toIndex));
        return new PageImpl<>(content, pageable, total);
    }

    // 상품 엔티티를 검색 결과 요약 DTO로 변환한다.
    private ProductSearchResponse.ProductSummary mapToProductSummary(Product product) {
        BigDecimal lowestPrice = extractLowestPrice(product);
        boolean hasStock = product.getProductOptions().stream()
            .map(ProductOption::getInventory)
            .filter(Objects::nonNull)
            .anyMatch(inventory -> inventory.getStockQuantity() != null
                && inventory.getStockQuantity().getValue() > 0);

        String thumbnailUrl = product.getImages().stream()
            .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
            .map(image -> image.getImageUrl())
            .findFirst()
            .orElse(null);

        return ProductSearchResponse.ProductSummary.builder()
            .productId(product.getProductId())
            .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
            .brandName(product.getBrandName())
            .productName(product.getProductName())
            .productInfo(product.getProductInfo())
            .productGenderType(product.getProductGenderType() != null
                ? product.getProductGenderType().name()
                : null)
            .isAvailable(product.getIsAvailable())
            .hasStock(hasStock)
            .lowestPrice(lowestPrice)
            .thumbnailUrl(thumbnailUrl)
            .categoryPath(product.getCategoryPath())
            .build();
    }

    private ProductGenderType parseGenderType(String gender) {
        try {
            return ProductGenderType.valueOf(gender.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 상품 성별 타입입니다.");
        }
    }
}
