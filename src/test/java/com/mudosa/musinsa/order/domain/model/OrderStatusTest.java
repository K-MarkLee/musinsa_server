package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderStatusTest {

    @Test
    @DisplayName("PENDING → COMPLETED 전이 성공")
    void pending_Complete_Success() {
        // given
        OrderStatus status = OrderStatus.PENDING;

        // when
        OrderStatus result = status.complete();

        // then
        assertThat(result).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("PENDING → CANCELLED 전이 성공")
    void pending_Cancel_Success() {
        // given
        OrderStatus status = OrderStatus.PENDING;

        // when
        OrderStatus result = status.cancel();

        // then
        assertThat(result).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("PENDING → REFUNDED 전이 시도 시 예외 발생")
    void pending_Refund_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.PENDING;

        // when & then
        assertThatThrownBy(status::refund)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("결제 대기 상태에서는 환불할 수 없습니다");
    }

    @Test
    @DisplayName("PENDING → PENDING 롤백 시 상태 유지")
    void pending_Rollback_RemainsInPending() {
        // given
        OrderStatus status = OrderStatus.PENDING;

        // when
        OrderStatus result = status.rollbackToPending();

        // then
        assertThat(result).isEqualTo(OrderStatus.PENDING);
    }


    @Test
    @DisplayName("COMPLETED → COMPLETED 전이 시도 시 예외 발생")
    void completed_Complete_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.COMPLETED;

        // when & then
        assertThatThrownBy(status::complete)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("이미 완료된 주문입니다");
    }

    @Test
    @DisplayName("COMPLETED → CANCELLED 전이 성공")
    void completed_Cancel_Success() {
        // given
        OrderStatus status = OrderStatus.COMPLETED;

        // when
        OrderStatus result = status.cancel();

        // then
        assertThat(result).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("COMPLETED → REFUNDED 전이 성공")
    void completed_Refund_Success() {
        // given
        OrderStatus status = OrderStatus.COMPLETED;

        // when
        OrderStatus result = status.refund();

        // then
        assertThat(result).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("COMPLETED → PENDING 롤백 성공")
    void completed_Rollback_Success() {
        // given
        OrderStatus status = OrderStatus.COMPLETED;

        // when
        OrderStatus result = status.rollbackToPending();

        // then
        assertThat(result).isEqualTo(OrderStatus.PENDING);
    }


    @Test
    @DisplayName("CANCELLED → COMPLETED 전이 시도 시 예외 발생")
    void cancelled_Complete_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::complete)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("주문 취소")
            .hasMessageContaining("완료");
    }

    @Test
    @DisplayName("CANCELLED → CANCELLED 전이 시도 시 예외 발생")
    void cancelled_Cancel_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::cancel)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("주문 취소")
            .hasMessageContaining("취소");
    }

    @Test
    @DisplayName("CANCELLED → REFUNDED 전이 시도 시 예외 발생")
    void cancelled_Refund_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::refund)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("주문 취소")
            .hasMessageContaining("환불");
    }

    @Test
    @DisplayName("CANCELLED → PENDING 롤백 시도 시 예외 발생")
    void cancelled_Rollback_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.CANCELLED;

        // when & then
        assertThatThrownBy(status::rollbackToPending)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("주문 취소")
            .hasMessageContaining("롤백");
    }


    @Test
    @DisplayName("REFUNDED → COMPLETED 전이 시도 시 예외 발생")
    void refunded_Complete_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.REFUNDED;

        // when & then
        assertThatThrownBy(status::complete)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("환불 완료")
            .hasMessageContaining("완료");
    }

    @Test
    @DisplayName("REFUNDED → CANCELLED 전이 시도 시 예외 발생")
    void refunded_Cancel_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.REFUNDED;

        // when & then
        assertThatThrownBy(status::cancel)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("환불 완료")
            .hasMessageContaining("취소");
    }

    @Test
    @DisplayName("REFUNDED → REFUNDED 전이 시도 시 예외 발생")
    void refunded_Refund_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.REFUNDED;

        // when & then
        assertThatThrownBy(status::refund)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("환불 완료")
            .hasMessageContaining("환불");
    }

    @Test
    @DisplayName("REFUNDED → PENDING 롤백 시도 시 예외 발생")
    void refunded_Rollback_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.REFUNDED;

        // when & then
        assertThatThrownBy(status::rollbackToPending)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION)
            .hasMessageContaining("환불 완료")
            .hasMessageContaining("롤백");
    }
}
