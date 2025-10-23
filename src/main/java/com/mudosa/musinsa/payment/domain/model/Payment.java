package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 결제 애그리거트 루트
 */
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
    private Long orderId; // Order 애그리거트 참조 (ID만, 1:1)
    
    @Column(name = "payment_method_id", nullable = false)
    private Integer paymentMethodId;
    
    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus; // status_code 참조
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "pg_provider", nullable = false, length = 50)
    private String pgProvider;
    
    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // 결제 로그 (같은 애그리거트)
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentLog> paymentLogs = new ArrayList<>();
    
    /**
     * 결제 생성
     */
    public static Payment create(
        Long orderId,
        Integer paymentMethodId,
        BigDecimal amount,
        String pgProvider
    ) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paymentMethodId = paymentMethodId;
        payment.amount = amount;
        payment.pgProvider = pgProvider;
        payment.paymentStatus = 1; // PENDING
        payment.currency = "KRW";
        return payment;
    }
    
    /**
     * 결제 승인
     */
    public void approve(Long userId, String pgTransactionId) {
        this.paymentStatus = 2; // APPROVED
        this.pgTransactionId = pgTransactionId;
        this.approvedAt = LocalDateTime.now();
        
        this.addLog(userId, "APPROVED", "결제 승인 완료");
    }
    
    /**
     * 결제 취소
     */
    public void cancel(Long userId, String reason) {
        this.paymentStatus = 3; // CANCELLED
        this.cancelledAt = LocalDateTime.now();
        
        this.addLog(userId, "CANCELLED", "결제 취소: " + reason);
    }
    
    /**
     * 결제 로그 추가
     */
    private void addLog(Long userId, String eventStatus, String description) {
        PaymentLog log = PaymentLog.create(userId, eventStatus, description);
        this.paymentLogs.add(log);
        log.assignPayment(this);
    }
}
