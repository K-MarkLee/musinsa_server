package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review")
public class Review extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_product_id", nullable = false)
    private OrderProduct orderProduct;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    

    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<ReviewImage> reviewImages = new java.util.ArrayList<>();
    
    @Builder
    public Review(OrderProduct orderProduct, Long userId, String content, Integer rating) {
        // 엔티티 기본 무결성 검증
        if (orderProduct == null) {
            throw new IllegalArgumentException("주문 상품은 리뷰에 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 리뷰에 필수입니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5 사이여야 합니다.");
        }
        
        this.orderProduct = orderProduct;
        this.userId = userId;
        this.content = content;
        this.rating = rating;
    }

}