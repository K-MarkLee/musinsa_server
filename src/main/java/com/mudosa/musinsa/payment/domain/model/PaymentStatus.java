package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("결제 대기"),

    APPROVED("결제 승인"),

    FAILED("결제 실패"),

    CANCELLED("결제 취소"),

    PARTIAL_CANCELLED("부분 취소");

    private final String description;

    public boolean canTransitionTo(PaymentStatus nextStatus) {
        return switch (this) {
            case PENDING -> nextStatus == APPROVED || nextStatus == FAILED;
            case APPROVED -> nextStatus == CANCELLED || nextStatus == PARTIAL_CANCELLED;
            case FAILED -> nextStatus == PENDING;  // 재시도 가능
            case CANCELLED, PARTIAL_CANCELLED -> false;
        };
    }

    public PaymentStatus transitionTo(PaymentStatus nextStatus) {
        if (!canTransitionTo(nextStatus)) {
            throw new BusinessException(
                    ErrorCode.INVALID_PAYMENT_STATUS,
                    String.format("%s → %s 전이는 허용되지 않습니다", this, nextStatus)
            );
        }
        return nextStatus;
    }

    public boolean isPending() {

        return this == PENDING;
    }

}
