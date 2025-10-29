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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Image> images = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<ProductOption> productOptions = new java.util.ArrayList<>();

    // CartItem과 ProductLike는 연결하지 않음 (성능 및 독립성 고려)
    // Review는 나중에 구현 예정 (현재 제거)

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
        
        this.brand = brand;
        this.productName = productName;
        this.productInfo = productInfo;
        this.productGenderType = productGenderType;
        this.brandName = brandName;
        this.categoryPath = categoryPath;
        this.isAvailable = isAvailable != null ? isAvailable : true;
        
        // 이미지 관계 설정
        this.images = images != null ? images : new java.util.ArrayList<>();
        if (images != null) {
            // 각 이미지에 상품 참조 설정
            images.forEach(image -> image.setProduct(this));
        }
        
        // 옵션 관계 설정
        this.productOptions = productOptions != null ? productOptions : new java.util.ArrayList<>();
        if (productOptions != null) {
            // 각 옵션에 상품 참조 설정
            productOptions.forEach(option -> option.setProduct(this));
        }
    }
}