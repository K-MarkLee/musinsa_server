package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.ProductGenderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 애그리거트 루트
 */
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

    // 카테고리 조인 테이블(`product_category`)과 좋아요(`product_like`) 대응
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<ProductCategory> productCategories = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private java.util.List<ProductLike> productLikes = new java.util.ArrayList<>();

    // CartItem은 직접 연결하지 않음 (성능 및 독립성 고려)
    // Review는 나중에 구현 예정

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "product_info", nullable = false, columnDefinition = "TEXT")
    private String productInfo;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_gender_type", nullable = false))
    private ProductGenderType productGenderType;

    // 역정규화 브랜드이름 (조회)
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName; 

    // 역정규화: "상의/티셔츠"
    @Column(name = "category_path", nullable = false, length = 255)
    private String categoryPath;

    
    /**
     * 상품 생성 (Builder 패턴)
     */
   @Builder
    public Product(Brand brand, String productName, String productInfo,
                   ProductGenderType productGenderType, String brandName, String categoryPath, Boolean isAvailable,
                   java.util.List<Image> images,
                   java.util.List<ProductOption> productOptions) {
        // 엔티티 기본 무결성 검증
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
            validateThumbnailConstraint(images);
            images.forEach(this::addImage);
        }

        if (productOptions != null) {
            productOptions.forEach(this::addProductOption);
        }
    }

    // 썸네일은 하나만 허용되므로 서비스에서 검증 없이 호출 가능하도록 보호
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

    public void addProductOption(ProductOption productOption) {
        if (productOption == null) {
            return;
        }
        productOption.setProduct(this);
        this.productOptions.add(productOption);
    }

    // 카테고리 서비스에서 내려준 엔티티를 그대로 연결
    public void addCategory(Category category) {
        if (category == null) {
            return;
        }
        ProductCategory mapping = ProductCategory.builder()
            .product(this)
            .category(category)
            .build();
        this.productCategories.add(mapping);
    }

    // 이미 생성된 매핑을 재활용할 때 사용 (예: bulk load)
    public void addProductCategory(ProductCategory productCategory) {
        if (productCategory == null) {
            return;
        }
        productCategory.assignProduct(this);
        this.productCategories.add(productCategory);
    }

    public void addProductLike(ProductLike productLike) {
        if (productLike == null) {
            return;
        }
        productLike.assignProduct(this);
        this.productLikes.add(productLike);
    }

    public boolean hasThumbnail() {
        return this.images.stream().anyMatch(image -> Boolean.TRUE.equals(image.getIsThumbnail()));
    }

    private void validateThumbnailConstraint(java.util.List<Image> images) {
        long thumbnailCount = images.stream()
            .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
            .count();
        if (thumbnailCount == 0) {
            throw new IllegalArgumentException("상품 썸네일은 최소 1개 이상이어야 합니다.");
        }
        if (thumbnailCount > 1) {
            throw new IllegalArgumentException("상품 썸네일은 하나만 등록할 수 있습니다.");
        }
    }
}