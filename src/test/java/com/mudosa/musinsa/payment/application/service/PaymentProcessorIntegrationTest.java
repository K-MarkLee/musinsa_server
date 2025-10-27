package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.payment.application.dto.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.PaymentConfirmResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("결제 프로세서 통합 테스트")
class PaymentProcessorIntegrationTest {

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private PaymentStrategyFactory strategyFactory;


    @Test
    @DisplayName("[통합] 전략 팩토리가 올바른 전략을 선택")
    void strategyFactory_SelectsCorrectStrategy() {
        // when
        PaymentStrategy tossStrategy = strategyFactory.getStrategy("TOSS");
        PaymentStrategy kakaoStrategy = strategyFactory.getStrategy("KAKAO");

        // then
        assertThat(tossStrategy).isNotNull();
        assertThat(tossStrategy.getProviderName()).isEqualTo("TOSS");
        assertThat(tossStrategy).isInstanceOf(TossPaymentStrategy.class);

        assertThat(kakaoStrategy).isNotNull();
        assertThat(kakaoStrategy.getProviderName()).isEqualTo("KAKAO");
        assertThat(kakaoStrategy).isInstanceOf(KakaoFakePaymentStrategy.class);
    }
}
