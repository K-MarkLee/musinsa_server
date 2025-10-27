package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 로그 엔티티
 * Payment 애그리거트 내부
 */
@Entity
@Table(name = "payment_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_log_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "event_status", nullable = false, length = 50)
    private String eventStatus; // 이벤트 상태 (CREATED, APPROVED, CANCELLED 등)
    
    @Column(name = "event_message", columnDefinition = "TEXT")
    private String eventMessage; // 이벤트 메시지
    
    /**
     * 결제 로그 생성
     */
    public static PaymentLog create(
            Payment payment,
            String eventStatus, 
            String eventMessage,
            Long userId) {
        PaymentLog log = new PaymentLog();
        log.payment = payment;
        log.userId = userId;
        log.eventStatus = eventStatus;
        log.eventMessage = eventMessage;
        return log;
    }
    
    /**
     * Payment 할당 (Package Private)
     */
    void assignPayment(Payment payment) {
        this.payment = payment;
    }
}
