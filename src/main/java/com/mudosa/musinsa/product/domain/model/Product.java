package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품 애그리거트 루트 엔티티로 연관된 하위 요소를 함께 관리한다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    // 이미지·옵션은 상품 생명주기와 동일하게 관리하기 위해 orphanRemoval 적용
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<Image> images = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<ProductOption> productOptions = new java.util.ArrayList<>();

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_info", nullable = false, columnDefinition = "TEXT")
    private String productInfo;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_gender_type", nullable = false)
    private ProductGenderType productGenderType;

    // 역정규화 브랜드이름 (조회)
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    // 역정규화: "상의/티셔츠"
    @Column(name = "category_path", nullable = false, length = 255)
    private String categoryPath;

    // 필수 값 검증 후 상품과 연관 컬렉션을 초기화하는 빌더 생성자이다.
    @Builder
    public Product(Brand brand, String productName, String productInfo,
                   ProductGenderType productGenderType, String brandName, String categoryPath, Boolean isAvailable,
                   java.util.List<Image> images,
                   java.util.List<ProductOption> productOptions) {
        // 필수 파라미터를 점검해 무결성을 보장한다.
        if (brand == null) {
            throw new IllegalArgumentException("브랜드는 필수입니다.");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (productInfo == null || productInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 정보는 필수입니다.");
        }
        if (productGenderType == null) {
            throw new IllegalArgumentException("상품 성별 타입은 필수입니다.");
        }
        if (brandName == null || brandName.trim().isEmpty()) {
            throw new IllegalArgumentException("브랜드명은 필수입니다.");
        }
        if (categoryPath == null || categoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 경로는 필수입니다.");
        }

        this.brand = brand;
        this.productName = productName;
        this.productInfo = productInfo;
        this.productGenderType = productGenderType;
        this.brandName = brandName;
        this.categoryPath = categoryPath;
        this.isAvailable = isAvailable != null ? isAvailable : true;

        if (images != null) {
            validateImages(images);
            images.forEach(this::addImage);
        }

        if (productOptions != null) {
            productOptions.forEach(this::addProductOption);
        }
    }

    // 단일 이미지를 추가하면서 썸네일 중복 여부를 검증한다.
    public void addImage(Image image) {
        if (image == null) {
            return;
        }
        if (Boolean.TRUE.equals(image.getIsThumbnail()) && hasThumbnail()) {
            throw new IllegalStateException("상품 썸네일은 하나만 설정할 수 있습니다.");
        }
        image.setProduct(this);
        this.images.add(image);
    }

    // 옵션 엔티티를 상품과 연결하고 컬렉션에 추가한다.
    public void addProductOption(ProductOption productOption) {
        if (productOption == null) {
            return;
        }
        validateOptionCombination(productOption);
        productOption.setProduct(this);
        this.productOptions.add(productOption);
    }

    // 옵션을 상품과의 연관에서 제거하고 고아 객체로 처리한다.
    public void removeProductOption(ProductOption productOption) {
        if (productOption == null) {
            return;
        }
        this.productOptions.remove(productOption);
        productOption.detachFromProduct();
    }

    // 이미지 리스트를 교체 등록하고 썸네일 조건을 확인한다.
    public void registerImages(java.util.List<ImageRegistration> imageRegistrations) {
        if (imageRegistrations == null || imageRegistrations.isEmpty()) {
            throw new IllegalArgumentException("상품 이미지는 최소 1장 이상 등록해야 합니다.");
        }

    java.util.List<Image> newImages = imageRegistrations.stream()
        .map(spec -> Image.builder()
            .product(this)
            .imageUrl(spec.imageUrl())
            .isThumbnail(spec.isThumbnail())
            .build())
        .toList();

    validateImages(newImages);

        this.images.clear();
        newImages.forEach(this::addImage);
    }

    // 상품 판매 가능 여부를 직접 전환한다.
    public void changeAvailability(boolean available) {
        this.isAvailable = available;
    }

    // 전달된 값이 존재할 때만 갱신하고, 값이 달라졌을 때 true를 반환한다.
    public boolean updateBasicInfo(String productName,
                                   String productInfo,
                                   ProductGenderType productGenderType) {
        boolean updated = false;

        if (productName != null) {
            if (productName.trim().isEmpty()) {
                throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
            }
            if (!productName.equals(this.productName)) {
                this.productName = productName;
                updated = true;
            }
        }

        if (productInfo != null) {
            if (productInfo.trim().isEmpty()) {
                throw new IllegalArgumentException("상품 정보는 비어 있을 수 없습니다.");
            }
            if (!productInfo.equals(this.productInfo)) {
                this.productInfo = productInfo;
                updated = true;
            }
        }

        if (productGenderType != null && productGenderType != this.productGenderType) {
            this.productGenderType = productGenderType;
            updated = true;
        }

        return updated;
    }

    // 현재 이미지 중 썸네일이 존재하는지 확인한다.
    public boolean hasThumbnail() {
        return this.images.stream().anyMatch(image -> Boolean.TRUE.equals(image.getIsThumbnail()));
    }

    // 이미지 목록에서 썸네일 개수를 검증한다.
    private void validateImages(java.util.List<Image> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("상품 이미지는 최소 1장 이상 등록해야 합니다.");
        }

        long thumbnailCount = images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
                .count();
        if (thumbnailCount != 1) {
            throw new IllegalArgumentException("상품 썸네일은 정확히 한 장이어야 합니다.");
        }
    }

    private void validateOptionCombination(ProductOption candidate) {
        java.util.List<Long> candidateIds = candidate.normalizedOptionValueIds();
        if (candidateIds.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "상품 옵션은 최소 1개의 옵션 값을 가져야 합니다.");
        }

        boolean duplicated = this.productOptions.stream()
                .map(ProductOption::normalizedOptionValueIds)
                .anyMatch(existing -> existing.equals(candidateIds));

        if (duplicated) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "동일한 옵션 조합이 이미 등록되어 있습니다.");
        }
    }

    // 이미지 등록 정보를 나타내는 레코드 클래스이다. 서비스에서 이미지 엔티티 직접 생성하지 않도록 한다.
    public record ImageRegistration(String imageUrl, boolean isThumbnail) {
    }
}