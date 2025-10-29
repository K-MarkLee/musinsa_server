package com.mudosa.musinsa.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_category")
public class ProductCategory {
    
    @EmbeddedId
    private ProductCategoryId id;
    
    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    
    @Builder
    public ProductCategory(Product product, Category category) {
        // 엔티티 기본 무결성 검증
        if (product == null) {
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        
    this.product = product;
    this.category = category;
    refreshIdentifiers();
    }
    
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class ProductCategoryId implements Serializable {
        
        @Column(name = "product_id")
        private Long productId;
        
        @Column(name = "category_id")
        private Long categoryId;
        
        public ProductCategoryId(Long productId, Long categoryId) {
            this.productId = productId;
            this.categoryId = categoryId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductCategoryId that = (ProductCategoryId) o;
            return Objects.equals(productId, that.productId) &&
                   Objects.equals(categoryId, that.categoryId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productId, categoryId);
        }
    }

    // 서비스에서 연관을 붙일 때 식별자 동기화를 보장하기 위한 헬퍼
    void assignProduct(Product product) {
        this.product = product;
        refreshIdentifiers();
    }

    void assignCategory(Category category) {
        this.category = category;
        refreshIdentifiers();
    }

    void refreshIdentifiers() {
        Long productId = this.product != null ? this.product.getProductId() : null;
        Long categoryId = this.category != null ? this.category.getCategoryId() : null;
        this.id = new ProductCategoryId(productId, categoryId);
    }
}