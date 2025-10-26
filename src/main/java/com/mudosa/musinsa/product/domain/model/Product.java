package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.CategoryPath;
import com.mudosa.musinsa.product.domain.vo.ProductName;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_product_brand_id", columnList = "brand_id"),
    @Index(name = "idx_product_created_at", columnList = "created_at DESC"),
    @Index(name = "ft_product_name_info", columnList = "product_name,product_info")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "brand_id", nullable = false)  // FK (외부 도메인)
    private Long brandId;

    @Column(name = "brand_name", nullable = false, length = 100) // 비정규화된 브랜드명
    private String brandName;

    @Embedded
    private ProductName productName;

    @Column(name = "product_info", nullable = false, columnDefinition = "TEXT")
    private String productInfo;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_gender_type", nullable = false)
    private GenderType productGenderType;

    @Embedded
    private CategoryPath categoryPath; // 비정규화된 카테고리 경로    

    // 연관관계 - ProductOption 연결 (상위 엔티티에서 하위 관리 필요)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();

    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Image> images = new ArrayList<>();

    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ProductLike> productLikes = new ArrayList<>();

    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ProductCategory> productCategories = new ArrayList<>();

    // 생성 메서드
    public static Product create(Long brandId, String brandName, String productName, String productInfo, 
                                GenderType productGenderType, String categoryPath) {
        return new Product(
            brandId,
            brandName,
            ProductName.of(productName),
            productInfo,
            true,  // isAvailable
            productGenderType,
            CategoryPath.of(categoryPath)
        );
    }

    // 비즈니스 메서드
    public void updateProductInfo(String productName, String productInfo, GenderType productGenderType) {
        this.productName = ProductName.of(productName);
        this.productInfo = productInfo;
        this.productGenderType = productGenderType;
    }

    public void updateBrandInfo(Long brandId, String brandName) {
        this.brandId = brandId;
        this.brandName = brandName;
    }

    public void updateCategoryPath(String categoryPath) {
        this.categoryPath = CategoryPath.of(categoryPath);
    }

    public void activate() {
        this.isAvailable = true;
    }

    public void deactivate() {
        this.isAvailable = false;
    }

    // 연관관계 메서드 - ProductOption 연결
    public void addProductOption(ProductOption productOption) {
        productOptions.add(productOption);
    }

    public void removeProductOption(ProductOption productOption) {
        productOptions.remove(productOption);
    }

    // 아직 구현되지 않은 엔티티들은 주석 처리
    /*
    public void addImage(Image image) {
        images.add(image);
    }

    public void addProductLike(ProductLike productLike) {
        productLikes.add(productLike);
    }

    public void addProductCategory(ProductCategory productCategory) {
        productCategories.add(productCategory);
    }
    */

    // 비즈니스 메서드 - 최저가 옵션 조회
    public ProductOption getCheapestOption() {
        return productOptions.stream()
            .min(Comparator.comparing(po -> po.getProductPrice().getValue()))
            .orElse(null);
    }

    // JPA를 위한 protected 생성자
    protected Product(Long brandId, String brandName, ProductName productName, String productInfo, 
                     Boolean isAvailable, GenderType productGenderType, 
                     CategoryPath categoryPath) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.productName = productName;
        this.productInfo = productInfo;
        this.isAvailable = isAvailable;
        this.productGenderType = productGenderType;
        this.categoryPath = categoryPath;
    }
}