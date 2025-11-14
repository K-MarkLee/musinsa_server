package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 상품 애그리거트 기본 접근을 담당하며 복잡한 조회는 커스텀 리포지토리로 위임한다.
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

  @Query("""
    select distinct p
    from Product p
    left join fetch p.brand
    left join fetch p.images
    where p.productId = :productId
    """)
  Optional<Product> findDetailById(Long productId);

  List<Product> findTop6ByBrandOrderByCreatedAtDesc(Brand brand);
}