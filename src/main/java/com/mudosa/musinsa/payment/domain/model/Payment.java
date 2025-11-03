package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Payment extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";

    @Column(name = "method", length = 50)
    private String method;  // "카드", "계좌이체", "간편결제" 등
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "pg_provider", nullable = false, length = 50)
    private String pgProvider;
    
    @Column(name = "pg_transaction_id", length = 100, unique = true)
    private String pgTransactionId;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentLog> paymentLogs = new ArrayList<>();

    public static Payment create(
            Long orderId,
            BigDecimal amount,
            String pgProvider,
            Long userId) {

        // 검증
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.pgProvider = pgProvider;
        payment.status = PaymentStatus.PENDING;
        payment.currency = "KRW";

        // 로그 추가
        payment.addLog(PaymentEventType.CREATED, "결제 생성", userId);

        return payment;
    }


    public void validatePending() {
        log.info(this.status.name());
        if (!this.status.isPending()) {
            throw new BusinessException(
                    ErrorCode.INVALID_PAYMENT_STATUS,
                    String.format("결제 상태가 PENDING이 아닙니다. 현재: %s", this.status)
            );
        }
    }

    public void validateAmount(BigDecimal requestAmount) {
        if (this.amount.compareTo(requestAmount) != 0) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_AMOUNT_MISMATCH,
                    String.format("결제 금액 불일치. 요청: %s, 실제: %s", requestAmount, this.amount)
            );
        }
    }

    private void addLog(PaymentEventType eventType, String message, Long userId) {
        PaymentLog log = PaymentLog.create(this, eventType, message, userId);
        this.paymentLogs.add(log);
    }

    /* 결제 승인 요청 */
    public void requestApproval(Long userId) {
        validatePending();
        addLog(PaymentEventType.APPROVAL_REQUESTED, "결제 승인 요청", userId);
    }

    /* 결제 승인 */
    public void approve(String pgTransactionId, Long userId, LocalDateTime approvedAt, String method) {
        if (pgTransactionId == null || pgTransactionId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PG_TRANSACTION_ID);
        }

        this.status = this.status.transitionTo(PaymentStatus.APPROVED);
        this.pgTransactionId = pgTransactionId;
        this.approvedAt = approvedAt;
        this.method = method;

        addLog(
                PaymentEventType.APPROVED,
                String.format("결제 승인 완료 - PG TID: %s", pgTransactionId),
                userId
        );
    }


    /* 결제 실패 */
    public void fail(String errorMessage, Long userId) {
        this.status = this.status.transitionTo(PaymentStatus.FAILED);

        addLog(
                PaymentEventType.FAILED,
                String.format("결제 실패: %s", errorMessage),
                userId
        );
    }

}
