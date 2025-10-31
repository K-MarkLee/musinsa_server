package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_category_parent"))
    private Category parent;
    
    // 자식 카테고리 컬렉션을 관리한다.
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Category> children = new ArrayList<>();
    
    // 카테고리를 생성하면서 필수 정보를 검증한다.
    @Builder
    public Category(String categoryName, Category parent, String imageUrl) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }
        
        this.categoryName = categoryName;
        this.parent = parent;
        this.imageUrl = imageUrl;
    }

    // 현재 카테고리의 전체 경로를 재귀적으로 생성한다.
    public String buildPath() {
        return buildPathInternal(new HashSet<>());
    }

    // 순환 검사를 하며 부모 경로를 이어 붙인다.
    private String buildPathInternal(Set<Category> visited) {
        if (!visited.add(this)) {
            throw new IllegalStateException("카테고리 계층에 순환 참조가 감지되었습니다.");
        }
        try {
            if (parent == null) {
                return categoryName;  // 부모: "상의"
            }
            return parent.buildPathInternal(visited) + "/" + categoryName;  // 자식: "상의/티셔츠"
        } finally {
            visited.remove(this);
        }
    }
    
    // 부모 카테고리가 존재하는지 확인한다.
    public boolean hasParent() {
        return this.parent != null;
    }
    
    // 루트 카테고리 여부를 확인한다.
    public boolean isRoot() {
        return this.parent == null;
    }

}