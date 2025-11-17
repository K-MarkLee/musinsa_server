package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "image")
public class Image extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;
    
    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail;
    
    // 이미지를 생성하며 필수 정보를 검증한다.
    public static Image create(String imageUrl, boolean isThumbnail) {
        return new Image(imageUrl, isThumbnail);
    }

    @Builder
    private Image(Product product, String imageUrl, Boolean isThumbnail) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }

    Image(String imageUrl, boolean isThumbnail) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_REQUIRED);
        }
        this.imageUrl = imageUrl;   
        this.isThumbnail = isThumbnail;
    }

    // 상품과의 연관관계를 설정한다.
    void setProduct(Product product) {
        this.product = product;
    }

}