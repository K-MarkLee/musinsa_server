package com.mudosa.musinsa.event.presentation.dto.res;

import com.mudosa.musinsa.event.service.EventCouponService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor

public class EventCouponIssueResDto {
    private Long memberCouponId;
    private Long couponId;

    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;
    private boolean duplicated;
    private String message; // 클라이언트에 표시할 메시지


    public static EventCouponIssueResDto from(EventCouponService.EventCouponIssueResult r){
        String message = r.duplicate()
                ? "이미 발급받은 쿠폰입니다."
                : "쿠폰이 발급되었습니다!";

        return EventCouponIssueResDto.builder()
                .memberCouponId(r.memberCouponId())
                .couponId(r.couponId())
                .issuedAt(r.issuedAt())
                .expiredAt(r.expiredAt())
                .duplicated(r.duplicate())
                .message(message)
                .build();
    }

}
