package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.CategoryPath;
import com.mudosa.musinsa.product.domain.vo.ProductGenderType;
import com.mudosa.musinsa.product.domain.vo.ProductName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_name", nullable = false, length = 100))
    private ProductName productName;
    
    @Column(name = "product_info", nullable = false, columnDefinition = "TEXT")
    private String productInfo;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_gender_type", nullable = false))
    private ProductGenderType productGenderType;
    
    // 비정규화 브랜드이름 (조회)
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName; 
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "category_path", nullable = false, length = 255))
    private CategoryPath categoryPath;
    
    @Builder
    public Product(Long brandId, String productName, String productInfo, 
                   Boolean isAvailable, ProductGenderType.Type genderType, 
                   String brandName, String categoryPath) {
        this.brandId = brandId;
        this.productName = new ProductName(productName);
        this.productInfo = productInfo;
        this.isAvailable = isAvailable != null ? isAvailable : true;
        this.productGenderType = new ProductGenderType(genderType);
        // 역정규화 필드: 생성 시점에 외부에서 받아서 저장
        this.brandName = brandName;
        this.categoryPath = categoryPath != null ? new CategoryPath(categoryPath) : null;
    }
    
    // 도메인 로직: 수정 (역정규화 필드는 직접 수정 불가)
    public void modify(String productName, String productInfo, ProductGenderType.Type genderType, Boolean isAvailable) {
        if (productName != null) this.productName = new ProductName(productName);
        if (productInfo != null) this.productInfo = productInfo;
        if (genderType != null) this.productGenderType = new ProductGenderType(genderType);
        if (isAvailable != null) this.isAvailable = isAvailable;
    }
}