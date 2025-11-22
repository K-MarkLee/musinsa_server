package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @DisplayName("결제 생성 시 결제 상태는 PENDING이다.")
    @Test
    void create(){
        //when
        Payment payment = Payment.create(1L, BigDecimal.valueOf(10000), PgProvider.TOSS, 1L);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @DisplayName("결제를 생성할 때 생성되는 PaymentLog의 상태는 CREATED이다.")
    @Test
    void createPaymentLog(){
        //when
        Payment payment = Payment.create(1L, BigDecimal.valueOf(10000), PgProvider.TOSS, 1L);

        //then
        assertThat(payment.getPaymentLogs().get(0).getEventStatus()).isEqualTo(PaymentEventType.CREATED);
    }

    @DisplayName("결제 승인 성공시 변경되는 결제 상태는 APPROVED이다.")
    @Test
    void updatePaymentStatusWhenPaymentConfirm(){
        //given
        Payment payment = Payment.create(1L, BigDecimal.valueOf(10000), PgProvider.TOSS, 1L);

        LocalDateTime now = LocalDateTime.now();
        //when
        payment.approve("pg-1", 1L, now, "카드");

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @DisplayName("결제 승인 성공시 생성되는 PaymentLog의 상태는 COMPLETED이다.")
    @Test
    void createPaymentLongWhenPaymentConfirm(){
        //given
        Payment payment = Payment.create(1L, BigDecimal.valueOf(10000), PgProvider.TOSS, 1L);

        LocalDateTime now = LocalDateTime.now();

        //when
        payment.approve("pg-1", 1L, now, "카드");

        //then
        assertThat(payment.getPaymentLogs()).extracting(pl->pl.getEventStatus()).containsExactly(PaymentEventType.CREATED, PaymentEventType.APPROVED);
    }

    @Test
    @DisplayName("주문 id가 없으면 예외를 발생한다.")
    void createWithNullOrderIdThrowsException() {
        assertThatThrownBy(() ->
                Payment.create(null, BigDecimal.valueOf(10000), PgProvider.TOSS, 1L)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("주문 ID는 필수입니다");
    }

    @Test
    @DisplayName("결제 금액이 null이면 예외를 발생한다")
    void createWithNullAmountThrowsException() {
        assertThatThrownBy(() ->
                Payment.create(1L, null, PgProvider.TOSS, 1L)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("결제 금액은 필수입니다");
    }

    @Test
    @DisplayName("PG사가 null이면 예외를 발생한다")
    void createWithNullPgProviderThrowsException() {
        assertThatThrownBy(() ->
                Payment.create(1L, BigDecimal.valueOf(10000), null, 1L)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PG사는 필수입니다");
    }

    @Test
    @DisplayName("사용자 Id가 null이면 예외를 발생한다")
    void createWithNullUserIdThrowsException() {
        assertThatThrownBy(() ->
                Payment.create(1L, BigDecimal.valueOf(10000), PgProvider.TOSS, null)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자 ID는 필수입니다");
    }

    @Test
    @DisplayName("결제 금액이 0 이하면 예외를 발생한다")
    void createWithZeroOrNegativeAmountThrowsException() {
        assertThatThrownBy(() ->
                Payment.create(1L, BigDecimal.valueOf(0), PgProvider.TOSS, 1L)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("결제 금액은 0보다 커야 합니다");
    }

}