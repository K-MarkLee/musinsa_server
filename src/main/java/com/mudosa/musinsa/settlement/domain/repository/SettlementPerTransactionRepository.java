package com.mudosa.musinsa.settlement.domain.repository;

import com.mudosa.musinsa.settlement.domain.model.SettlementPerTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 거래별 정산 데이터 조회
 */
@Repository
public interface SettlementPerTransactionRepository
    extends JpaRepository<SettlementPerTransaction, Long>,
            SettlementPerTransactionAggregationRepository {

    /**
     * OOM 방지를 위해 페이징 사용
     */
    Page<SettlementPerTransaction> findByBrandId(Long brandId, Pageable pageable);

    /**
     * 특정 브랜드의 정산 집계 대상 거래를 조회 (날짜 범위 + 페이징)
     */
    Page<SettlementPerTransaction> findByBrandIdAndTransactionDateLocalBetween(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    );

    /**
     * 특정 결제 ID에 연결된 모든 정산 거래를 조회
     */
    List<SettlementPerTransaction> findByPaymentId(Long paymentId);
}
