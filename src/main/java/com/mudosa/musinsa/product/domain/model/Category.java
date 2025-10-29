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
    public Category(String categoryName, Category parent, String imageUrl) {
        // 엔티티 기본 무결성 검증
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }

        this.categoryName = categoryName;
        this.parent = parent;
        this.imageUrl = imageUrl;
    }

    // 도메인 로직: 경로 생성 (재귀)
    public String buildPath() {
        if (parent == null) {
            return categoryName;  // 부모: "상의"
        }
        return parent.buildPath() + "/" + categoryName;  // 자식: "상의/티셔츠"
    }

    // 도메인 로직: 하위 카테고리 여부 확인
    public boolean hasParent() {
        return this.parent != null;
    }

    // 도메인 로직: 상위 카테고리 여부 확인
    public boolean isRoot() {
        return this.parent == null;
    }

}