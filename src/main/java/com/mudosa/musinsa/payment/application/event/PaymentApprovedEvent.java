package com.mudosa.musinsa.payment.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 승인 완료 이벤트
 * - 결제 승인 후 발행
 */
@Getter
@RequiredArgsConstructor
public class PaymentApprovedEvent {
    private final Long paymentId;
    private final String pgTransactionId;
    private final Long userId;
}