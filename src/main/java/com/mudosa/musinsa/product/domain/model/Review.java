package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품 리뷰를 표현하는 엔티티로 사용자와 주문 상품을 연결한다.
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;    
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    

    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<ReviewImage> reviewImages = new java.util.ArrayList<>();
    
    // 리뷰를 생성하며 필수 정보를 검증한다.
    @Builder
    public Review(OrderProduct orderProduct, User user, String content, Integer rating) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (orderProduct == null) {
            throw new IllegalArgumentException("주문 상품은 리뷰에 필수입니다.");
        }
        if (user == null) {
            throw new IllegalArgumentException("사용자 ID는 리뷰에 필수입니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5 사이여야 합니다.");
        }
        
        this.orderProduct = orderProduct;
        this.user = user;
        this.content = content;
        this.rating = rating;
    }

}