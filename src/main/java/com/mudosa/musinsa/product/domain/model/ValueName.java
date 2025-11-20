package com.mudosa.musinsa.product.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ValueName {
    SIZE("사이즈"),
    COLOR("색상");

    private final String name;
}
