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
    private Integer paymentStatus; // StatusCode FK
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // 결제 금액
    
    @Column(name = "pg_provider", nullable = false, length = 50)
    private String pgProvider; // PG사 (토스, 카카오페이 등)
    
    @Column(name = "pg_transaction_id", length = 100, unique = true)
    private String pgTransactionId; // PG사 거래 ID
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 승인 일시
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 취소 일시
    
    // 결제 로그 (같은 애그리거트)
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentLog> paymentLogs = new ArrayList<>();
    
    /**
     * 결제 생성
     */
    public static Payment create(
        Long orderId,
        Integer paymentMethodId,
        Integer paymentStatus,
        BigDecimal amount,
        String pgProvider
    ) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paymentMethodId = paymentMethodId;
        payment.paymentStatus = paymentStatus;
        payment.amount = amount;
        payment.pgProvider = pgProvider;
        payment.currency = "KRW";
        return payment;
    }
    
    /**
     * 결제 승인
     */
    public void approve(String pgTransactionId, Integer approvedStatusId) {
        this.paymentStatus = approvedStatusId;
        this.pgTransactionId = pgTransactionId;
        this.approvedAt = LocalDateTime.now();
    }
    
    /**
     * 결제 취소
     */
    public void cancel(Integer cancelledStatusId) {
        this.paymentStatus = cancelledStatusId;
        this.cancelledAt = LocalDateTime.now();
    }
    
    /**
     * 결제 로그 추가
     */
    public void addLog(PaymentLog log) {
        this.paymentLogs.add(log);
        log.assignPayment(this);
    }
    
    /**
     * 상태 변경
     */
    public void changeStatus(Integer newStatus) {
        this.paymentStatus = newStatus;
    }
}
