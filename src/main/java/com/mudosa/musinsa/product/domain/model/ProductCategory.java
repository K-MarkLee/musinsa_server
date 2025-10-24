package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품-카테고리 매핑 엔티티
 * Product 애그리거트 내부
 */
@Entity
@Table(name = "product_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_category_id")
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    /**
     * 상품-카테고리 생성
     */
    public static ProductCategory create(Long productId, Long categoryId) {
        ProductCategory pc = new ProductCategory();
        pc.productId = productId;
        pc.categoryId = categoryId;
        return pc;
    }
}
