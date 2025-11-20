package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("결제 대기 중") {
        @Override
        public OrderStatus complete() {
            return COMPLETED;
        }

        @Override
        public OrderStatus cancel() {
            return CANCELLED;
        }

        @Override
        public OrderStatus refund() {
            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "결제 대기 상태에서는 환불할 수 없습니다"
            );
        }

        @Override
        public OrderStatus rollbackToPending() {
            return this;
        }
    },

    COMPLETED("결제 완료") {
        @Override
        public OrderStatus complete() {
            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "이미 완료된 주문입니다"
            );
        }

        @Override
        public OrderStatus cancel() {
            return CANCELLED;
        }

        @Override
        public OrderStatus refund() {
            return REFUNDED;
        }

        @Override
        public OrderStatus rollbackToPending() {
            return PENDING;
        }
    },

    CANCELLED("주문 취소") {
        @Override
        public OrderStatus complete() {
            throw invalidTransition("완료");
        }

        @Override
        public OrderStatus cancel() {
            throw invalidTransition("취소");
        }

        @Override
        public OrderStatus refund() {
            throw invalidTransition("환불");
        }

        @Override
        public OrderStatus rollbackToPending() {
            throw invalidTransition("롤백");
        }
    },

    REFUNDED("환불 완료") {
        @Override
        public OrderStatus complete() {
            throw invalidTransition("완료");
        }

        @Override
        public OrderStatus cancel() {
            throw invalidTransition("취소");
        }

        @Override
        public OrderStatus refund() {
            throw invalidTransition("환불");
        }

        @Override
        public OrderStatus rollbackToPending() {
            throw invalidTransition("롤백");
        }
    };

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public abstract OrderStatus complete();
    public abstract OrderStatus cancel();
    public abstract OrderStatus refund();
    public abstract OrderStatus rollbackToPending();

    protected BusinessException invalidTransition(String action) {
        return new BusinessException(
                ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                String.format("%s 상태에서는 %s할 수 없습니다", this.description, action)
        );
    }
}
