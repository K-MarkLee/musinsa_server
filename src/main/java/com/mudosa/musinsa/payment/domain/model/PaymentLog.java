package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    /* 결제 로그 생성 */
    public static PaymentLog create(
            Payment payment,
            PaymentEventType eventType,
            String eventMessage,
            Long userId) {

        PaymentLog log = new PaymentLog();
        log.payment = payment; //Payment 어그리게이트 처리할 수 있도록 꼭 설정해줘야함.
        log.eventStatus = eventType;
        log.eventMessage = eventMessage;
        log.userId = userId;
        return log;
    }


    void assignPayment(Payment payment) {
        this.payment = payment;
    }


}
