package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, ProductOptionValue.ProductOptionValueId> {
    @Query("""
        SELECT pov
        FROM ProductOptionValue pov
        JOIN FETCH pov.optionValue ov
        WHERE pov.productOption.productOptionId IN :productOptionIds
    """)
    List<ProductOptionValue> findAllByProductOptionIdsWithOptionValue(List<Long> productOptionIds);
}
