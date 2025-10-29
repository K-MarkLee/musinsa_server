package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_image")
public class ReviewImage extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long reviewImageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;
    
    @Builder
    public ReviewImage(Review review, String imageUrl) {
        // 엔티티 기본 무결성 검증
        if (review == null) {
            throw new IllegalArgumentException("리뷰는 필수입니다.");
        }
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        
        this.review = review;
        this.imageUrl = imageUrl;
    }

}