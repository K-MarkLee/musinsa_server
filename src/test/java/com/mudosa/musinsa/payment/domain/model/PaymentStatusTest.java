package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PaymentStatusTest {

    @Test
    @DisplayName("PENDING → APPROVED 전이 성공")
    void pending_Approve_Success() {
        // given
        PaymentStatus status = PaymentStatus.PENDING;

        // when
        PaymentStatus result = status.approve();

        // then
        assertThat(result).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("PENDING → FAILED 전이 성공")
    void pending_Fail_Success() {
        // given
        PaymentStatus status = PaymentStatus.PENDING;

        // when
        PaymentStatus result = status.fail();

        // then
        assertThat(result).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("PENDING → CANCELLED 전이 시도 시 예외 발생")
    void pending_Cancel_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.PENDING;

        // when & then
        assertThatThrownBy(status::cancel)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 대기")
            .hasMessageContaining("취소");
    }



    @Test
    @DisplayName("APPROVED → APPROVED 전이 시도 시 예외 발생")
    void approved_Approve_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.APPROVED;

        // when & then
        assertThatThrownBy(() -> status.approve())
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 승인")
            .hasMessageContaining("승인");
    }

    @Test
    @DisplayName("APPROVED → FAILED 전이 시도 시 예외 발생")
    void approved_Fail_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.APPROVED;

        // when & then
        assertThatThrownBy(() -> status.fail())
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 승인")
            .hasMessageContaining("실패");
    }

    @Test
    @DisplayName("APPROVED → CANCELLED 전이 성공")
    void approved_Cancel_Success() {
        // given
        PaymentStatus status = PaymentStatus.APPROVED;

        // when
        PaymentStatus result = status.cancel();

        // then
        assertThat(result).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("FAILED → APPROVED 전이 시도 시 예외 발생")
    void failed_Approve_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.FAILED;

        // when & then
        assertThatThrownBy(status::approve)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 실패")
            .hasMessageContaining("승인");
    }

    @Test
    @DisplayName("FAILED → FAILED 전이 시도 시 예외 발생")
    void failed_Fail_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.FAILED;

        // when & then
        assertThatThrownBy(status::fail)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 실패")
            .hasMessageContaining("실패");
    }

    @Test
    @DisplayName("FAILED → CANCELLED 전이 시도 시 예외 발생")
    void failed_Cancel_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.FAILED;

        // when & then
        assertThatThrownBy(status::cancel)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 실패")
            .hasMessageContaining("취소");
    }


    @Test
    @DisplayName("FAILED → PENDING 재시도 성공")
    void failed_RetryPending_Success() {
        // given
        PaymentStatus status = PaymentStatus.FAILED;

        // when
        PaymentStatus result = status.rollback();

        // then
        assertThat(result).isEqualTo(PaymentStatus.PENDING);
    }


    @Test
    @DisplayName("CANCELLED → APPROVED 전이 시도 시 예외 발생")
    void cancelled_Approve_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::approve)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 취소")
            .hasMessageContaining("승인");
    }

    @Test
    @DisplayName("CANCELLED → FAILED 전이 시도 시 예외 발생")
    void cancelled_Fail_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::fail)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 취소")
            .hasMessageContaining("실패");
    }

    @Test
    @DisplayName("CANCELLED → CANCELLED 전이 시도 시 예외 발생")
    void cancelled_Cancel_ThrowsException() {
        // given
        PaymentStatus status = PaymentStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::cancel)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PAYMENT_STATUS)
            .hasMessageContaining("결제 취소")
            .hasMessageContaining("취소");
    }
}
