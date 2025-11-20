package com.mudosa.musinsa.coupon.domain.service;

import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CouponListService {

    private final MemberCouponRepository memberCouponRepository;

    /**
     * 사용자가 발급받은 모든 쿠폰 목록 조회
     * @param userId 사용자 ID
     * @return 발급된 쿠폰 목록
     */
    public List<MemberCoupon> getMemberCoupons(Long userId) {
        log.info("사용자 쿠폰 목록 조회 - userId: {}", userId);
        return memberCouponRepository.findAllByUserId(userId);
    }

    /**
     * 사용자가 발급받은 사용 가능한 쿠폰 목록만 조회
     * @param userId 사용자 ID
     * @return 사용 가능한 쿠폰 목록
     */
    public List<MemberCoupon> getAvailableMemberCoupons(Long userId) {
        log.info("사용자 사용 가능 쿠폰 목록 조회 - userId: {}", userId);
        List<MemberCoupon> allCoupons = memberCouponRepository.findAllByUserId(userId);

        return allCoupons.stream()
                .filter(MemberCoupon::isUsuable)
                .toList();
    }
}
