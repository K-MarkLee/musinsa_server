package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Category Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByParentId(Long parentId);
    
    List<Category> findByParentIdIsNull();
    
    List<Category> findByCategoryLevel(Integer level);
}
