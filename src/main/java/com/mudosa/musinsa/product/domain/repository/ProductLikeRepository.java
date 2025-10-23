package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductLike Repository
 */
@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
    
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);
    
    List<ProductLike> findByUserId(Long userId);
    
    List<ProductLike> findByProductId(Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
