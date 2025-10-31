package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    // 특정 상품과 사용자 조합의 좋아요를 조회. (사용자가 선택한 상품 좋아요 상태 확인용)
    Optional<ProductLike> findByProductAndUserId(Product product, Long userId);

    // 상품의 좋아요 수를 집계.
    long countByProduct(Product product);
}
