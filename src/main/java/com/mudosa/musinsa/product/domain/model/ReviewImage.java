package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 리뷰에 첨부된 이미지를 나타내는 엔티티이다.
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
    
    // 리뷰 이미지를 생성하며 필수 값을 검증한다.
    @Builder
    public ReviewImage(Review review, String imageUrl) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
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