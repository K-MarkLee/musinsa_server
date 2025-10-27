package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.ImageUrl;

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
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_image_product"))
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", foreignKey = @ForeignKey(name = "fk_image_event"))
    private Long event;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "image_url", nullable = false, length = 2048))
    private ImageUrl imageUrl;
    
    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail;
    
    @Builder
    public Image(Product product, Long event, ImageUrl imageUrl, Boolean isThumbnail) {
        this.product = product;
        this.event = event;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail != null ? isThumbnail : false;
    }
    
    // 도메인 로직: 통합 수정
    public void modify(Product product, Long event, ImageUrl imageUrl, Boolean isThumbnail) {
        if (product != null) this.product = product;
        if (event != null) this.event = event;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (isThumbnail != null) this.isThumbnail = isThumbnail;
    }
    
    // 도메인 로직: 썸네일 여부 확인
    public boolean isThumbnailImage() {
        return Boolean.TRUE.equals(this.isThumbnail);
    }
    
    // 도메인 로직: 상품 이미지 여부 확인
    public boolean isProductImage() {
        return this.product != null;
    }
    

}