package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyFactoryTest {
    @Mock
    private PaymentStrategy tossStrategy;

    @Mock
    private PaymentStrategy kakaoStrategy;

    private PaymentStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PaymentStrategyFactory(List.of(tossStrategy, kakaoStrategy));
    }

    @DisplayName("조건에 맞는 전략을 반환한다.")
    @Test
    void getStrategyReturnsMatchingStrategy(){
        //given
        PaymentContext context = PaymentContext.builder()
                .pgProvider(PgProvider.TOSS)
                .amount(BigDecimal.valueOf(50000))
                .paymentType(PaymentType.NORMAL)
                .build();

        when(tossStrategy.supports(context)).thenReturn(true);
        lenient().when(kakaoStrategy.supports(context)).thenReturn(true);

        //when
        PaymentStrategy result = factory.getStrategy(context);

        //then
        assertThat(result).isEqualTo(tossStrategy);
        verify(tossStrategy, times(1)).supports(context);
    }

    @DisplayName("조건에 맞는 전략이 없으면 예외를 발생한다. ")
    @Test
    void getStrategyNoMatchThrowsException(){
        //given
        PaymentContext context = PaymentContext.builder()
                .pgProvider(PgProvider.NAVER)
                .amount(BigDecimal.valueOf(50000))
                .paymentType(PaymentType.NORMAL)
                .build();

        when(tossStrategy.supports(any())).thenReturn(false);
        when(kakaoStrategy.supports(any())).thenReturn(false);

        //when
        assertThatThrownBy(() -> factory.getStrategy(context))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_STRATEGY_NOT_FOUND)
                .hasMessageContaining("결제 전략을 찾을 수 없습니다");
    }

    @DisplayName("여러 전략이 조건을 만족하면 첫 번째 전략을 반환한다. ")
    @Test
    void getStrategyMultipleMatchesReturnsFirst(){
        PaymentContext context = PaymentContext.builder()
                .pgProvider(PgProvider.TOSS)
                .amount(BigDecimal.valueOf(50000))
                .paymentType(PaymentType.NORMAL)
                .build();

        when(tossStrategy.supports(context)).thenReturn(true);
        lenient().when(kakaoStrategy.supports(context)).thenReturn(true);

        // when
        PaymentStrategy result = factory.getStrategy(context);

        // then
        assertThat(result).isEqualTo(tossStrategy);  // 첫 번째 전략
    }
}