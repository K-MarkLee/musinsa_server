package com.mudosa.musinsa.coupon.domain.service;

import com.mudosa.musinsa.coupon.domain.model.Coupon;
import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.OrderMemberCoupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberCouponService {
    private final MemberCouponRepository memberCouponRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long userId, Long couponId, BigDecimal orderAmount) {
        log.info("쿠폰 할인 계산 시작 - userId: {}, couponId: {}, orderAmount: {}",
                userId, couponId, orderAmount);

        // 회원 쿠폰 조회
        MemberCoupon memberCoupon = findMemberCoupon(userId, couponId);

        // 회원 쿠폰 사용 가능 여부 검증 (상태 + 만료일)
        memberCoupon.validateUsable();

        // 쿠폰 유효성 검증 + 할인 금액 계산
        Coupon coupon = memberCoupon.getCoupon();
        BigDecimal discountAmount = coupon.validateAndCalculateDiscount(orderAmount);

        log.info("쿠폰 할인 계산 완료 - userId: {}, couponId: {}, discount: {}",
                userId, couponId, discountAmount);

        return discountAmount;
    }

    @Transactional
    public void useMemberCoupon(Long userId, Long couponId, Long orderId) {
        log.info("쿠폰 사용 처리 시작 - userId: {}, couponId: {}, orderId: {}",
                userId, couponId, orderId);

        // 회원 쿠폰 조회
        MemberCoupon memberCoupon = findMemberCoupon(userId, couponId);

        // 쿠폰 사용 처리
        memberCoupon.use(orderId);
        memberCouponRepository.save(memberCoupon);

        log.info("쿠폰 사용 처리 완료 - userId: {}, couponId: {}, orderId: {}",
                userId, couponId, orderId);
    }

    private MemberCoupon findMemberCoupon(Long userId, Long couponId) {
        return memberCouponRepository
                .findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COUPON_NOT_FOUND,
                        String.format("쿠폰을 찾을 수 없습니다 - userId: %d, couponId: %d", userId, couponId)
                ));
    }


    @Transactional(propagation = Propagation.MANDATORY)
    public void rollbackMemberCoupon(Long userId, Long couponId, Long orderId) {
        //회원 쿠폰 조회
        MemberCoupon memberCoupon = findMemberCoupon(userId, couponId);

        // 쿠폰이 이 주문에서 사용되었는지 확인
        if (!orderId.equals(memberCoupon.getUsedOrderId())) {
            throw new BusinessException(
                    ErrorCode.COUPON_ROLLBACK_INVALID,
                    "쿠폰이 다른 주문에서 사용되어 롤백할 수 없습니다"
            );
        }

        // 쿠폰 상태를 사용 가능으로 복구
        memberCoupon.rollbackUsage();
        memberCouponRepository.save(memberCoupon);

        log.info("쿠폰 롤백 완료 - userId: {}, couponId: {}", userId, couponId);
    }

    public List<OrderMemberCoupon> findMemberCoupons(Long userId) {
        return memberCouponRepository.findAllByUserId(userId).stream()
                .filter(MemberCoupon::isUsuable)
                .map(mc -> {
                    Coupon coupon = mc.getCoupon();
                    return new OrderMemberCoupon(
                            coupon.getId(),
                            coupon.getCouponName(),
                            coupon.getDiscountType().name(),
                            coupon.getDiscountValue()
                    );
                }).toList();
    }
}
