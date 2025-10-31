package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품에 대한 좋아요를 저장하는 엔티티이다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_like")
public class ProductLike extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_like_id")
    private Long productLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // 좋아요 엔티티를 생성하면서 필수 값을 검증한다.
    @Builder
    public ProductLike(Product product, Long userId) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (product == null) {
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        
        this.product = product;
        this.userId = userId;
    }
    
    // 해당 좋아요가 전달된 상품에 속하는지 판별한다.
    public boolean belongsToProduct(Product product) {
        return this.product != null && this.product.equals(product);
    }

    // 상품 애그리거트에 이미 생성된 좋아요를 재연결한다.
    void assignProduct(Product product) {
        this.product = product;
    }
}