package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentContextTest {
    @DisplayName("PaymentConfirmRequest를 PaymentContext로 변환한다")
    @Test
    void from(){
        //given
        PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                .orderNo("NON_EXISTENT_ORDER")
                .paymentKey("test_key")
                .amount(20000L)
                .build();

        //when
        PaymentContext result = PaymentContext.from(request);

        //then
        assertThat(result.getPaymentType()).isEqualTo(PaymentType.NORMAL);

    }
}