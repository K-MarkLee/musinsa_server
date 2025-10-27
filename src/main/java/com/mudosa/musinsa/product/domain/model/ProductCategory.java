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
    private Product productId;
    
    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category categoryId;
    
    @Builder
    public ProductCategory(Product productId, Category categoryId) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.id = new ProductCategoryId(
            productId.getProductId(),
            categoryId.getCategoryId()
        );
    }
    
    /**
     * 상품-카테고리 매핑 정보 수정
     * @param product 수정할 상품
     * @param category 수정할 카테고리
     */
    public void modify(Product productId, Category categoryId) {
        if (productId != null) {
            this.productId = productId;
            this.id.productId = productId.getProductId();
        }
        if (categoryId != null) {
            this.categoryId = categoryId;
            this.id.categoryId = categoryId.getCategoryId();
        }
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
}