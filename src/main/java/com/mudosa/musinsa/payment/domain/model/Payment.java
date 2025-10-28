package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;
    
    @Column(name = "payment_method_id", nullable = false)
    private Integer paymentMethodId;
    
    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";
    
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


    public void validatePending() {
        if (this.paymentStatus != 1) {
            throw new IllegalStateException("이미 처리된 결제입니다. 현재 상태: " + this.paymentStatus);
        }
    }

    public void validateAmount(BigDecimal requestAmount) {
        if (this.amount.compareTo(requestAmount) != 0) {
            throw new IllegalArgumentException(
                String.format("결제 금액이 일치하지 않습니다. 요청: %s, 실제: %s", 
                    requestAmount, this.amount)
            );
        }
    }

    public void approve(String pgTransactionId, LocalDateTime approvedAt) {
        this.paymentStatus = 2; // APPROVED
        this.pgTransactionId = pgTransactionId;
        this.approvedAt = approvedAt;
    }


    public void fail(String errorMessage) {
        this.paymentStatus = 3; // FAILED
        
        PaymentLog log = PaymentLog.create(
            this,
            "PAYMENT_FAILED",
            errorMessage,
            null
        );
        this.paymentLogs.add(log);
    }


    public void rollback() {
        this.paymentStatus = 1; // PENDING
        this.pgTransactionId = null;
        this.approvedAt = null;
    }

    public void cancel() {
        this.paymentStatus = 4; // CANCELLED (필요 시 추가)
        this.cancelledAt = LocalDateTime.now();
    }
}
