package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementPerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SettlementPerTransaction Repository
 */
@Repository
public interface SettlementPerTransactionRepository extends JpaRepository<SettlementPerTransaction, Long> {
    
    List<SettlementPerTransaction> findByBrandId(Long brandId);
    
    List<SettlementPerTransaction> findByPaymentId(Long paymentId);
}
