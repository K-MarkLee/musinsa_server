package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.CategoryName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "category_name", nullable = false, length = 100))
    private CategoryName categoryName;
    
    // 카테고리 깊이는 1 또는 2
    @Column(name = "category_depth", nullable = false)
    private Byte categoryDepth;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "image_url", length = 2048)
    private String imageUrl;
    
    @Builder
    public Category(String categoryName, Integer categoryDepth, Long parentId, String imageUrl) {
        this.categoryName = new CategoryName(categoryName);
        this.categoryDepth = categoryDepth != null ? categoryDepth.byteValue() : null;
        this.parentId = parentId;
        this.imageUrl = imageUrl;
    }
    
    // 도메인 로직: 생성 (Builder에서 처리)
    
    // 도메인 로직: 수정
    public void modify(String categoryName, Integer categoryDepth, Long parentId, String imageUrl) {
        if (categoryName != null) this.categoryName = new CategoryName(categoryName);
        if (categoryDepth != null) this.categoryDepth = categoryDepth.byteValue();
        if (parentId != null) this.parentId = parentId;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }
    
    // 도메인 로직: 하위 카테고리 여부 확인
    public boolean hasParent() {
        return this.parentId != null;
    }
    
    // 도메인 로직: 상위 카테고리 여부 확인
    public boolean isRoot() {
        return this.parentId == null;
    }
    
    // 자기 참조: 부모 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_category_parent"))
    private Category parent;
    
    // 자기 참조: 자식 카테고리 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();
    
    // 도메인 로직: 자식 카테고리 추가
    public void addChild(Category child) {
        children.add(child);
        child.parent = this;  // 자식의 부모 설정
    }
}