package com.mudosa.musinsa.event.presentation.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class EventCouponIssueReqDto {

    //private Long productId;
    @NotNull(message = "상품 옵션 ID는 필수입니다.")
    private Long productOptionId;
    //private long eventId;


    //@NotNull(message = "사용자 ID는 필수입니다")
    //private Long userId;
    //@NotNull(message = "이벤트 상품 옵션 ID는 필수입니다")
    //private Long productOptionId;
    //@NotNull(message = "이벤트 ID는 필수입니다 ")
    //private Long eventId;


}
