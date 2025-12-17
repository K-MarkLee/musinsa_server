package com.mudosa.musinsa.order.application.dto.request;

import com.mudosa.musinsa.order.application.dto.OrderCreateItem;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
public class OrderCreateRequest {
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    private List<OrderCreateItem> items;

    private Long couponId; // 선택적

    @Builder
    private OrderCreateRequest(List<OrderCreateItem> items, Long couponId) {
        this.items = items;
        this.couponId = couponId;
    }
}
