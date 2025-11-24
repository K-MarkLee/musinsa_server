package com.mudosa.musinsa.order.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderListResponse {
    private List<OrderInfo> orders;
}
