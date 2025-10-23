package com.mudosa.musinsa.brand.domain.repository;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Brand Repository
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    List<Brand> findByStatus(BrandStatus status);
    
    boolean existsByNameKo(String nameKo);
}
