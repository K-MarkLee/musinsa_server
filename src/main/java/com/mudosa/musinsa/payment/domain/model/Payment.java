package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
public class Payment extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;
    
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String currency = "KRW";

    private String method;
    
    private BigDecimal amount;
    
    private String pgProvider;
    
    private String pgTransactionId;
    
    private LocalDateTime approvedAt;
    
    private LocalDateTime cancelledAt;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentLog> paymentLogs = new ArrayList<>();

    @Builder
    public Payment(Long orderId, PaymentStatus status, String currency, String method, BigDecimal amount, String pgProvider, String pgTransactionId, LocalDateTime approvedAt, LocalDateTime cancelledAt, List<PaymentLog> paymentLogs) {
        this.orderId = orderId;
        this.status = status;
        this.currency = currency;
        this.method = method;
        this.amount = amount;
        this.pgProvider = pgProvider;
        this.pgTransactionId = pgTransactionId;
        this.approvedAt = approvedAt;
        this.cancelledAt = cancelledAt;
        this.paymentLogs = paymentLogs;
    }

    public static Payment create(
            Long orderId,
            BigDecimal amount,
            String pgProvider,
            Long userId) {

        //결제 생성

        //결제 로그 추가

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
        if (!this.status.isPending()) {
            throw new BusinessException(
                    ErrorCode.INVALID_PAYMENT_STATUS,
                    String.format("결제 상태가 PENDING이 아닙니다. 현재: %s", this.status)
            );
        }
    }


    private void addLog(PaymentEventType eventType, String message, Long userId) {
        PaymentLog log = PaymentLog.create(this, eventType, message, userId);
        this.paymentLogs.add(log);
    }


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
