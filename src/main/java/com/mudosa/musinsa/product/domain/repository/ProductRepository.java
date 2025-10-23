package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByBrandId(Long brandId);
    
    List<Product> findByIsAvailable(Boolean isAvailable);
}
