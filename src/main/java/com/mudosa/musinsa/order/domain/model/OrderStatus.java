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
        public OrderStatus rollback() {
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
        public OrderStatus rollback() {
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
        public OrderStatus rollback() {
            return COMPLETED;
        }
    };

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public abstract OrderStatus complete();
    public abstract OrderStatus cancel();
    public abstract OrderStatus rollback();

    protected BusinessException invalidTransition(String action) {
        return new BusinessException(
                ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                String.format("%s 상태에서는 %s할 수 없습니다", this.description, action)
        );
    }

    public boolean isCancable() {
        return this == PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCancelled() { return this == CANCELLED;
    }
}
