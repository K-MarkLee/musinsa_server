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


    // 아직 없음
    public static EventCouponIssueResDto from(EventCouponService.EventCouponIssueResult r){
        return EventCouponIssueResDto.builder()
                .memberCouponId(r.memberCouponId())
                .couponId(r.couponId())
                .issuedAt(r.issuedAt())
                .expiredAt(r.expiredAt())
//                .duplicate(r.duplicate())
                .duplicated(r.duplicate())
                .build();
    }

}
