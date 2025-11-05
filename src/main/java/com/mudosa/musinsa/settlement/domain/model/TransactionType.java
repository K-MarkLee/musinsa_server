package com.mudosa.musinsa.settlement.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정산 거래 유형 (주문, 환불 등)
 * */
@Getter
@RequiredArgsConstructor
public enum TransactionType {

    ORDER("주문"),
    REFUND("환불");

    private final String description;
}
