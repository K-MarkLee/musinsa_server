package com.mudosa.musinsa.settlement.application;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.settlement.domain.model.SettlementPerTransaction;
import com.mudosa.musinsa.settlement.domain.model.TransactionType;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 정산 애플리케이션 서비스
 * - 정산 거래 생성 및 관리
 * - 정산 상태 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementApplicationService {

    private final SettlementPerTransactionRepository settlementPerTransactionRepository;

    /**
     * 결제 완료 시 정산 거래 생성
     *
     * @param brandId 브랜드 ID
     * @param paymentId 결제 ID
     * @param pgTransactionId PG사 거래 ID
     * @param transactionAmount 거래 금액
     * @param commissionRate 수수료율
     * @param pgFeeAmount PG 수수료
     * @param transactionType 거래 타입 (주문/환불)
     * @param timezoneOffset 타임존 오프셋
     * @return 생성된 정산 거래
     */
    @Transactional
    public SettlementPerTransaction createSettlementTransaction(
        Long brandId,
        Long paymentId,
        String pgTransactionId,
        Money transactionAmount,
        BigDecimal commissionRate,
        Money pgFeeAmount,
        TransactionType transactionType,
        String timezoneOffset
    ) {
        log.info("Creating settlement transaction for payment_id={}, amount={}", paymentId, transactionAmount);

        SettlementPerTransaction settlement = SettlementPerTransaction.create(
            brandId,
            paymentId,
            pgTransactionId,
            transactionAmount,
            commissionRate,
            pgFeeAmount,
            transactionType,
            timezoneOffset
        );

        SettlementPerTransaction saved = settlementPerTransactionRepository.save(settlement);

        log.info("Settlement transaction created: id={}, final_amount={}",
            saved.getId(), saved.calculateFinalSettlementAmount());

        return saved;
    }

    /**
     * 브랜드의 특정 기간 정산 거래 조회
     *
     * @param brandId 브랜드 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 정산 거래 목록
     */
    @Transactional(readOnly = true)
    public List<SettlementPerTransaction> getSettlementTransactions(
        Long brandId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        log.info("Fetching settlement transactions for brand_id={}, period={} to {}",
            brandId, startDate, endDate);

        return settlementPerTransactionRepository.findByBrandIdAndTransactionDateLocalBetween(brandId, startDate, endDate);
    }

    /**
     * 정산 거래 단건 조회
     *
     * @param settlementId 정산 ID
     * @return 정산 거래
     */
    @Transactional(readOnly = true)
    public SettlementPerTransaction getSettlementTransaction(Long settlementId) {
        return settlementPerTransactionRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));
    }

    /**
     * Payment ID로 정산 거래 조회
     *
     * @param paymentId 결제 ID
     * @return 정산 거래
     */
    @Transactional(readOnly = true)
    public SettlementPerTransaction getSettlementTransactionByPaymentId(Long paymentId) {
        return settlementPerTransactionRepository.findFirstByPaymentId(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Settlement not found for payment: " + paymentId));
    }
}