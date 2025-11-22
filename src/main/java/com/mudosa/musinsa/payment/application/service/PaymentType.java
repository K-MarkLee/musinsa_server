package com.mudosa.musinsa.payment.application.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentType {
    NORMAL("일반"),
    GLOBAL("해외"),
    REGULAR("정기");

    private final String description;
}
