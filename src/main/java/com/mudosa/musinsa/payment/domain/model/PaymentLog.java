package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    
    private Long userId;

    @Enumerated(EnumType.STRING)
    private PaymentEventType eventStatus;
    
    private String eventMessage;

    @Builder
    public PaymentLog(Payment payment, Long userId, PaymentEventType eventStatus, String eventMessage) {
        this.payment = payment;
        this.userId = userId;
        this.eventStatus = eventStatus;
        this.eventMessage = eventMessage;
    }

    public static PaymentLog create(
            Payment payment,
            PaymentEventType eventStatus,
            String eventMessage,
            Long userId) {
        return PaymentLog.builder()
                .eventStatus(eventStatus)
                .payment(payment)
                .eventMessage(eventMessage)
                .userId(userId)
                .build();
    }


}
