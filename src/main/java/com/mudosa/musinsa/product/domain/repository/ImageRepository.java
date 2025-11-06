package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.product.productId IN :productIds
        AND i.isThumbnail = true
    """)
    List<Image> findThumbnailsByProductIds(List<Long> productIds);
}
