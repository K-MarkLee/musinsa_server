package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_like")
public class ReviewLike extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_like_id")
    private Long reviewLikeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Builder
    public ReviewLike(Review review, User user) {
        this.review = review;
        this.user = user;
    }
    
    // 도메인 로직: 리뷰 변경
    public void changeReview(Review review) {
        if (review != null) this.review = review;
    }
    
    // 도메인 로직: 사용자 변경
    public void changeUser(User user) {
        if (user != null) this.user = user;
    }
    
    // 도메인 로직: 특정 리뷰의 좋아요 여부 확인
    public boolean belongsToReview(Review review) {
        return this.review != null && this.review.equals(review);
    }
    
    // 도메인 로직: 특정 사용자의 좋아요 여부 확인
    public boolean belongsToUser(User user) {
        return this.user != null && this.user.equals(user);
    }

}