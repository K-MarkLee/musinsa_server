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
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
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

    public static Payment create(
        Long orderId,
        Long userId,
        Integer paymentMethodId,
        Integer paymentStatus,
        BigDecimal amount,
        String pgProvider
    ) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.userId = userId;
        payment.paymentMethodId = paymentMethodId;
        payment.paymentStatus = paymentStatus;
        payment.amount = amount;
        payment.pgProvider = pgProvider;
        payment.currency = "KRW";
        return payment;
    }

    public void validatePending() {
        // TODO: PaymentStatus enum 또는 상수로 관리 필요
        // 임시로 1 = PENDING, 2 = APPROVED, 3 = FAILED로 가정
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
        
        // 실패 로그 추가
        PaymentLog log = PaymentLog.create(
            this,
            "PAYMENT_FAILED",
            errorMessage,
            this.userId
        );
        this.paymentLogs.add(log);
    }

}
