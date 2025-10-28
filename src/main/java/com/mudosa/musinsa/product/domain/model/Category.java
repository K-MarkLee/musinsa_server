package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리 애그리거트 루트
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
    
    // 카테고리 깊이는 1 또는 2
    @Column(name = "category_depth", nullable = false)
    private Byte categoryDepth;
    
    @Column(name = "image_url", length = 2048)
    private String imageUrl;
    
    // 자기 참조: 부모 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_category_parent"))
    private Category parent;
    
    // 자기 참조: 자식 카테고리 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Category> children = new ArrayList<>();
    
    /**
     * 카테고리 생성 (Builder 패턴)
     */
    @Builder
    public Category(String categoryName, Byte categoryDepth, Category parent, String imageUrl) {
        this.categoryName = categoryName;
        this.categoryDepth = categoryDepth;
        this.parent = parent;
        this.imageUrl = imageUrl;
    }
    
    // 도메인 로직: 정보 수정
    public void modify(String categoryName, Byte categoryDepth, String imageUrl) {
        if (categoryName != null) this.categoryName = categoryName;
        if (categoryDepth != null) this.categoryDepth = categoryDepth;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }
    
    // 도메인 로직: 부모 카테고리 변경
    public void changeParent(Category parent) {
        if (parent != null) this.parent = parent;
    }
    
    // 도메인 로직: 하위 카테고리 여부 확인
    public boolean hasParent() {
        return this.parent != null;
    }
    
    // 도메인 로직: 상위 카테고리 여부 확인
    public boolean isRoot() {
        return this.parent == null;
    }
    
    // 도메인 로직: 자식 카테고리 추가
    public void addChild(Category child) {
        children.add(child);
        child.parent = this;  // 자식의 부모 설정
    }
}