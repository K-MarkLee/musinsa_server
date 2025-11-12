package com.mudosa.musinsa.event.presentation.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class EventCouponIssueReqDto {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    @NotNull(message = "이벤트 옵션 ID는 필수입니다")
    private Long eventOptionId;
    @NotNull(message = "이벤트 ID는 필수입니다 ")
    private Long eventId;


}
