package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Inventory Repository
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProductOptionId(Long productOptionId);
    
    boolean existsByProductOptionId(Long productOptionId);
    
    /**
     * 비관적 락으로 재고 조회
     * - 동시성 문제 방지
     * - 재고 차감 시 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productOptionId = :productOptionId")
    Optional<Inventory> findByProductOptionIdWithLock(@Param("productOptionId") Long productOptionId);
}
