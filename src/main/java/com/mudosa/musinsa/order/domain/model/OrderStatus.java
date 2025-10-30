package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("결제 대기 중"),
    COMPLETED("결제 완료"),
    PREPARING("배송 준비 중"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소"),
    REFUNDED("환불 완료");

    private final String description;


    public boolean canTransitionTo(OrderStatus nextStatus) {
        return switch (this) {
            case PENDING -> nextStatus == COMPLETED || nextStatus == CANCELLED;
            case COMPLETED -> nextStatus == PREPARING || nextStatus == CANCELLED;
            case PREPARING -> nextStatus == SHIPPING || nextStatus == CANCELLED;
            case SHIPPING -> nextStatus == DELIVERED || nextStatus == CANCELLED;
            case DELIVERED -> nextStatus == REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
    }

    public OrderStatus transitionTo(OrderStatus nextStatus) {
        if (!canTransitionTo(nextStatus)) {
            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    String.format("%s → %s 전이는 허용되지 않습니다", this, nextStatus)
            );
        }
        return nextStatus;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    public boolean isSettleable() {
        return this == COMPLETED || this == PREPARING ||
                this == SHIPPING || this == DELIVERED;
    }

    public boolean isCancellable() {
        return this == PENDING || this == COMPLETED ||
                this == PREPARING || this == SHIPPING;
    }

}
