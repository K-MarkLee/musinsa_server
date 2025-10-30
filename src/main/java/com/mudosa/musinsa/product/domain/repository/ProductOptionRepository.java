package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 상품 옵션을 개별적으로 조회하고 관리하는 리포지토리이다.
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    // 특정 상품에 속한 옵션 목록을 조회한다.
    List<ProductOption> findAllByProduct(Product product);
}
