package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 애그리거트 루트
 */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId; // Brand 애그리거트 참조 (ID만)
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "product_info", nullable = false, columnDefinition = "TEXT")
    private String productInfo;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "product_gender_type", nullable = false)
    private ProductGenderType productGenderType;
    
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName; // 비정규화 (조회 성능)
    
    @Column(name = "category_path", nullable = false)
    private String categoryPath;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    // 상품 옵션 (같은 애그리거트)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();
    
    // 상품 이미지 (같은 애그리거트)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();
    
    /**
     * 상품 생성
     */
    public static Product create(
        Long brandId,
        String productName,
        String productInfo,
        ProductGenderType genderType,
        String brandName,
        String categoryPath
    ) {
        Product product = new Product();
        product.brandId = brandId;
        product.productName = productName;
        product.productInfo = productInfo;
        product.productGenderType = genderType;
        product.brandName = brandName;
        product.categoryPath = categoryPath;
        product.isAvailable = true;
        product.likeCount = 0;
        return product;
    }
    
    /**
     * 상품 옵션 추가
     */
    public void addOption(ProductOption option) {
        this.productOptions.add(option);
        option.assignProduct(this);
    }
    
    /**
     * 이미지 추가
     */
    public void addImage(Image image) {
        this.images.add(image);
        image.assignProduct(this);
    }
    
    /**
     * 좋아요 증가
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    /**
     * 좋아요 감소
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
