package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;

import com.mudosa.musinsa.event.domain.model.Event;
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
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail;

    @Builder
    public Image(Product product, Event event, String imageUrl, Boolean isThumbnail) {
        // 엔티티 기본 무결성 검증
        if (product == null) {
            throw new IllegalArgumentException("상품은 이미지에 필수입니다.");
        }
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }

        this.product = product;
        this.event = event;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail != null ? isThumbnail : false;
    }

    // 패키지 private: 상품 참조 설정 (Product 애그리거트에서만 사용)
    void setProduct(Product product) {
        this.product = product;
    }

}