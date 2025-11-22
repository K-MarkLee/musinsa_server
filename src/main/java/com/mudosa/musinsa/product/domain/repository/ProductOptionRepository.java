package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 상품 옵션을 개별적으로 조회하고 관리하는 리포지토리이다.
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long>,  ProductOptionRepositoryCustom {

    @Query("SELECT DISTINCT po FROM ProductOption po " +
           "JOIN FETCH po.inventory " +
           "WHERE po.productOptionId IN :ids")
    List<ProductOption> findAllByIdWithInventory(@Param("ids") List<Long> ids);

    @Query("SELECT po FROM ProductOption po " +
        "JOIN FETCH po.product " +
        "JOIN FETCH po.inventory " +
        "WHERE po.productOptionId = :id")
    Optional<ProductOption> findByIdWithProductAndInventory(@Param("id") Long id);
    
    // 특정 상품에 속한 옵션 목록을 조회한다.
    List<ProductOption> findAllByProduct(Product product);
    Optional<ProductOption> findByInventory(Inventory inventory);

    // 동일한 옵션 값 조합이 이미 존재하는지 확인한다.
    @Query("""
        select (count(po) > 0)
        from ProductOption po
        join po.productOptionValues pov
        where po.product.productId = :productId
        and pov.optionValue.optionValueId in (:sizeOptionValueId, :colorOptionValueId)
        group by po.productOptionId
        having count(distinct pov.optionValue.optionValueId) = 2
        """)
    boolean existsByProductIdAndOptionValueIds(@Param("productId") Long productId,
                                               @Param("sizeOptionValueId") Long sizeOptionValueId,
                                               @Param("colorOptionValueId") Long colorOptionValueId);
}
