package com.mudosa.musinsa.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

// 상품과 카테고리의 다대다 매핑을 담당하는 엔티티이다.
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

    
    // 상품과 카테고리 연관을 생성하며 필수 정보를 검증한다.
    @Builder
    public ProductCategory(Product product, Category category) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
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
        
        // 복합 키를 구성하는 생성자이다.
        public ProductCategoryId(Long productId, Long categoryId) {
            this.productId = productId;
            this.categoryId = categoryId;
        }
        
        // 동일 조합인지 비교한다.
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductCategoryId that = (ProductCategoryId) o;
            return Objects.equals(productId, that.productId) &&
                   Objects.equals(categoryId, that.categoryId);
        }
        
        // 복합 키 해시 코드를 계산한다.
        @Override
        public int hashCode() {
            return Objects.hash(productId, categoryId);
        }
    }

    // 상품을 재연결할 때 식별자를 동기화한다.
    void assignProduct(Product product) {
        this.product = product;
        refreshIdentifiers();
    }

    // 카테고리를 재연결할 때 식별자를 동기화한다.
    void assignCategory(Category category) {
        this.category = category;
        refreshIdentifiers();
    }

    // 현재 연관 상태에 맞춰 복합 키를 재구성한다.
    void refreshIdentifiers() {
        Long productId = this.product != null ? this.product.getProductId() : null;
        Long categoryId = this.category != null ? this.category.getCategoryId() : null;
        this.id = new ProductCategoryId(productId, categoryId);
    }
}