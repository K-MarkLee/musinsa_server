package com.mudosa.musinsa.coupon.presentation.controller;

import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import com.mudosa.musinsa.coupon.domain.service.CouponQueryService;
import com.mudosa.musinsa.coupon.presentation.dto.res.MemberCouponResDto;
import com.mudosa.musinsa.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponQueryService couponQueryService;

    /**
     * 사용자가 발급받은 모든 쿠폰 목록 조회
     * GET /api/coupons/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<MemberCouponResDto>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("사용자 쿠폰 목록 조회 요청 - userId: {}", user != null ? user.getUserId() : "null");

        List<MemberCoupon> memberCoupons = couponQueryService.getMemberCoupons(user.getUserId());

        List<MemberCouponResDto> response = memberCoupons.stream()
                .map(MemberCouponResDto::from)
                .collect(Collectors.toList());

        log.info("사용자 쿠폰 목록 조회 완료 - userId: {}, 쿠폰 개수: {}", user.getUserId(), response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자가 발급받은 사용 가능한 쿠폰 목록만 조회
     * GET /api/coupons/my/available
     */
    @GetMapping("/my/available")
    public ResponseEntity<List<MemberCouponResDto>> getMyAvailableCoupons(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("사용자 사용 가능 쿠폰 목록 조회 요청 - userId: {}", user != null ? user.getUserId() : "null");

        List<MemberCoupon> memberCoupons = couponQueryService.getAvailableMemberCoupons(user.getUserId());

        List<MemberCouponResDto> response = memberCoupons.stream()
                .map(MemberCouponResDto::from)
                .collect(Collectors.toList());

        log.info("사용자 사용 가능 쿠폰 목록 조회 완료 - userId: {}, 쿠폰 개수: {}", user.getUserId(), response.size());
        return ResponseEntity.ok(response);
    }
}
