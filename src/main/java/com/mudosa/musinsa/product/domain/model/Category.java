package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 애그리거트 루트
 */
@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "category_level", nullable = false)
    private Integer categoryLevel = 0;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    /**
     * 카테고리 생성
     */
    public static Category create(String categoryName, Long parentId, int level) {
        Category category = new Category();
        category.categoryName = categoryName;
        category.parentId = parentId;
        category.categoryLevel = level;
        return category;
    }
    
    /**
     * 최상위 카테고리 생성
     */
    public static Category createRoot(String categoryName) {
        return create(categoryName, null, 0);
    }
}
