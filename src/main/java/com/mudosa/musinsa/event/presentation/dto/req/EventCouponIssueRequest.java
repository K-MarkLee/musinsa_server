package com.mudosa.musinsa.event.presentation.dto.req;

import com.mudosa.musinsa.coupon.domain.model.DiscountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class EventCouponIssueRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    @NotNull(message = "이벤트 옵션 ID는 필수입니다")
    private Long eventOptionId;


}
