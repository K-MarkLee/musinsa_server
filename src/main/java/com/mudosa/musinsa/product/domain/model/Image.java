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
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
    
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;
    
    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail;
    
    // 이미지를 생성하며 필수 정보를 검증한다.
    @Builder
    public Image(Product product, Event event, String imageUrl, Boolean isThumbnail) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        
        // 최소한 하나의 참조는 필수
        if (product == null && event == null) {
            throw new IllegalArgumentException("상품 또는 이벤트 중 하나는 필수입니다.");
        }
        
        this.product = product;
        this.event = event;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail != null ? isThumbnail : false;
    }

    // 상품 애그리거트에서만 호출해 양방향 연관을 설정한다.
    void setProduct(Product product) {
        this.product = product;
    }

}