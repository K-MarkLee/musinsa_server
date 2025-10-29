package com.mudosa.musinsa.coupon.domain.service;

import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberCouponService {
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public void useMemberCoupon(Long userId, Long couponId, Long orderId) {
        log.info("쿠폰 사용 처리 시작 - userId: {}, couponId: {}, orderId: {}",
                userId, couponId, orderId);
        MemberCoupon memberCoupon = memberCouponRepository
                .findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        memberCoupon.use(orderId);

        memberCouponRepository.save(memberCoupon);

        log.info("쿠폰 사용 처리 완료 - userId: {}, couponId: {}, orderId: {}",
                userId, couponId, orderId);
    }
}
