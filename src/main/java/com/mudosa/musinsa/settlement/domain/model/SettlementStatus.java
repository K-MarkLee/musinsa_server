package com.mudosa.musinsa.settlement.domain.model;

import lombok.Getter;

/* 정산 상태 */
@Getter
public enum SettlementStatus {

    PENDING("대기중"),
    PROCESSING("처리중"),
    COMPLETED("정산완료"),
    FAILED("실패");

    private final String description;

    SettlementStatus(String description) {
        this.description = description;
    }

}
