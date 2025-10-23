package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 이미지 엔티티
 * Product 애그리거트 내부
 */
@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;
    
    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail = false;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    /**
     * 이미지 생성
     */
    public static Image create(String imageUrl, boolean isThumbnail, int displayOrder) {
        Image image = new Image();
        image.imageUrl = imageUrl;
        image.isThumbnail = isThumbnail;
        image.displayOrder = displayOrder;
        return image;
    }
    
    /**
     * Product 할당 (Package Private)
     */
    void assignProduct(Product product) {
        this.product = product;
    }
}
