package com.mudosa.musinsa.payment.application.service;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.common.client.ClientRequest;
import com.mudosa.musinsa.common.client.ClientResponse;
import com.mudosa.musinsa.common.client.ExternalApiClient;
import com.mudosa.musinsa.common.client.HttpHeadersBuilder;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.exception.ExternalApiException;
import com.mudosa.musinsa.payment.application.dto.PaymentResponseDto;
import com.mudosa.musinsa.payment.application.dto.request.PaymentConfirmRequest;
import com.mudosa.musinsa.payment.application.dto.response.TossPaymentConfirmResponse;
import com.mudosa.musinsa.payment.domain.model.PgProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TossPaymentStrategyTest extends ServiceConfig {
    @Autowired
    private TossPaymentStrategy tossPaymentStrategy;

    @MockBean
    private ExternalApiClient apiClient;

    @MockBean
    private HttpHeadersBuilder headersBuilder;

    @DisplayName("조건에 만족하는 결제 전략일 시 true를 반환한다. ")
    @Test
    void supportsValidConditionReturnsTrue(){
        //given
        PaymentContext context = PaymentContext.builder()
                .pgProvider(PgProvider.TOSS)
                .amount(BigDecimal.valueOf(50000))
                .paymentType(PaymentType.NORMAL)
                .build();

        //when
        boolean result = tossPaymentStrategy.supports(context);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("결제전략 조건에 안맞을 때 false를 반환한다.")
    @Test
    void supportsAmountExceedsReturnsFalse(){
        // given
        PaymentContext context = PaymentContext.builder()
                .pgProvider(PgProvider.TOSS)
                .amount(BigDecimal.valueOf(150000))
                .paymentType(PaymentType.NORMAL)
                .build();

        // when
        boolean result = tossPaymentStrategy.supports(context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("결제 승인 API 호출에 성공")
    void firstAttemptSuccessNoRetry() {
        // given
        PaymentConfirmRequest request = createPaymentConfirmRequest();

        when(headersBuilder.basicAuth(anyString()))
                .thenReturn(new HttpHeaders());

        LocalDateTime now = LocalDateTime.now();
        TossPaymentConfirmResponse mockResponse = createMockTossResponse(now);

        ClientResponse<TossPaymentConfirmResponse> successResponse =
                new ClientResponse<>(mockResponse, HttpStatus.OK, new HttpHeaders());

        when(apiClient.<TossPaymentConfirmResponse>execute(any(ClientRequest.class)))
                .thenReturn(successResponse);

        // when
        PaymentResponseDto result = tossPaymentStrategy.confirmPayment(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result)
                .extracting("paymentKey", "orderNo", "method", "totalAmount")
                        .contains("test_payment_key_123", "ORD_TEST_001", 50000L);

        verify(apiClient, times(1)).execute(any(ClientRequest.class));
    }

    @DisplayName("PG사에서 응답이 없을 때 예외를 발생한다.")
    @Test
    void confirmPaymentNullBodyThrowsBusinessException(){
        //given
        PaymentConfirmRequest request = createPaymentConfirmRequest();

        when(headersBuilder.basicAuth(anyString()))
                .thenReturn(new HttpHeaders());

        ClientResponse<TossPaymentConfirmResponse> successResponse =
                new ClientResponse<>(null, HttpStatus.OK, new HttpHeaders());

        when(apiClient.<TossPaymentConfirmResponse>execute(any(ClientRequest.class)))
                .thenReturn(successResponse);

        //when
        assertThatThrownBy(() -> tossPaymentStrategy.confirmPayment(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_APPROVAL_FAILED);

        verify(apiClient, times(1)).execute(any(ClientRequest.class));
    }

    @DisplayName("타임아웃 발생 시 재시도를 수행하지 않는다.")
    @Test
    void retryLogicTimeoutRetries(){
        //given
        PaymentConfirmRequest request = createPaymentConfirmRequest();
        when(headersBuilder.basicAuth(anyString()))
                .thenReturn(new HttpHeaders());

        when(apiClient.execute(any(ClientRequest.class)))
                .thenThrow(new ExternalApiException(
                        "API 타임아웃",
                        HttpStatus.REQUEST_TIMEOUT,
                        "요청 시간이 초과되었습니다",
                        null
                ));

        //when & then
        assertThatThrownBy(() ->
                tossPaymentStrategy.confirmPayment(request)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_TIMEOUT)
                .hasMessage("결제 처리 시간이 초과되었습니다");
        verify(apiClient, times(1)).execute(any(ClientRequest.class));
    }


    @DisplayName("재시도 로직을 모두 실패하면 예외를 발생한다")
    @Test
    void retryThreeTimesAndCallRecover(){
        //given
        PaymentConfirmRequest request = createPaymentConfirmRequest();
        when(headersBuilder.basicAuth(anyString()))
                .thenReturn(new HttpHeaders());
        
        //PG사 결제 승인 호출 항상 실패
        when(apiClient.execute(any(ClientRequest.class)))
                .thenThrow(new ExternalApiException(
                        "Toss API 서버 오류",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "{\"error\":\"server_error\"}",
                        null
                ));

        //when & then
        assertThatThrownBy(() ->
                tossPaymentStrategy.confirmPayment(request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Toss API 서버 오류");

        verify(apiClient, times(3)).execute(any(ClientRequest.class));
    }

    @DisplayName("2번째 시도에서 성공하여 재시도를 한번만 한다.")
    @Test
    void retryTwoTimes(){
        //given
        PaymentConfirmRequest request = createPaymentConfirmRequest();
        when(headersBuilder.basicAuth(anyString()))
                .thenReturn(new HttpHeaders());
        LocalDateTime now = LocalDateTime.now();
        TossPaymentConfirmResponse mockResponse = createMockTossResponse(now);
        ClientResponse<TossPaymentConfirmResponse> successResponse =
                new ClientResponse<>(mockResponse, HttpStatus.OK, new HttpHeaders());

        when(apiClient.<TossPaymentConfirmResponse>execute(any(ClientRequest.class)))
                .thenThrow(new ExternalApiException(
                        "Server Error",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        null,
                        null
                ))
                .thenReturn(successResponse);

        //when
        PaymentResponseDto result = tossPaymentStrategy.confirmPayment(request);

        //then
        assertThat(result).isNotNull();
        verify(apiClient, times(2)).execute(any(ClientRequest.class));
    }

    private PaymentConfirmRequest createPaymentConfirmRequest() {
        return PaymentConfirmRequest.builder()
                .paymentKey("test_payment_key_123")
                .orderNo("ORD_TEST_001")
                .amount(50000L)
                .build();
    }

    private TossPaymentConfirmResponse createMockTossResponse(LocalDateTime createdAt) {
        return TossPaymentConfirmResponse.builder()
                .paymentKey("test_payment_key_123")
                .orderId("ORD_TEST_001")
                .status("DONE")
                .method("카드")
                .totalAmount(50000L)
                .approvedAt(createdAt)
                .build();
    }
}