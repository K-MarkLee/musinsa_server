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
    @DisplayName("PENDING → PENDING 롤백 시 상태 유지")
    void pending_Rollback_RemainsInPending() {
        // given
        OrderStatus status = OrderStatus.PENDING;

        // when
        OrderStatus result = status.rollback();

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
    @DisplayName("COMPLETED → PENDING 롤백 성공")
    void completed_Rollback_Success() {
        // given
        OrderStatus status = OrderStatus.COMPLETED;

        // when
        OrderStatus result = status.rollback();

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
    @DisplayName("CANCELLED → COMPLETED 롤백 시도 시 예외 발생")
    void cancelled_Rollback_ThrowsException() {
        // given
        OrderStatus status = OrderStatus.CANCELLED;

        // when
        OrderStatus result = status.rollback();

        // then
        assertThat(result).isEqualTo(OrderStatus.COMPLETED);
    }

}
