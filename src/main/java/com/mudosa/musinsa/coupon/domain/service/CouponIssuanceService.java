package com.mudosa.musinsa.coupon.domain.service;

import com.mudosa.musinsa.coupon.domain.model.Coupon;
import com.mudosa.musinsa.coupon.domain.model.CouponProduct;
import com.mudosa.musinsa.coupon.domain.model.MemberCoupon;
import com.mudosa.musinsa.coupon.domain.repository.CouponRepository;
import com.mudosa.musinsa.coupon.domain.repository.MemberCouponRepository;
import com.mudosa.musinsa.coupon.domain.service.CouponProductService;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor // ✅ final 필드 주입용 생성자 자동 생성
@Transactional
@Slf4j
public class CouponIssuanceService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponProductService couponProductService;

    // 조회 전용
    @Transactional(readOnly = true)
    public Optional<CouponIssuanceResult> findIssuedCoupon(Long userId, Long couponId) {
        return memberCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .map(existing -> CouponIssuanceResult.duplicate(
                        existing.getId(),
                        couponId,
                        existing.getExpiredAt(),
                        existing.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public long countIssuedByUser(Long userId, Long couponId) {
        return memberCouponRepository.countByUserIdAndCouponId(userId, couponId);
    }


    @Transactional
    public CouponIssuanceResult issueCoupon(Long userId, Long couponId,Long productId){

        // 1. 쿠폰 조회 + 비관적 락 (동시성 제어)
        Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        // 2. 발급 가능 상태 검증 (활성화, 기간, 재고 확인)
        LocalDateTime now = LocalDateTime.now();
        coupon.validateIssuable(now);

//        // 상품 적용 가능 여부 검증
//        if(!couponProductService.isApplicableToProduct(couponId,productId)){
//            throw new BusinessException(
//                    ErrorCode.EVENT_PRODUCT_MISMATCH
//                    , "쿠폰이 해당 상품에 적용되지 않습니다"
//            );
//        }


        return memberCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .map(mc -> {
                    log.info("기존 발급 재사용 - userId: {}, couponId: {}", userId, couponId);
                    return CouponIssuanceResult.duplicate(
                            mc.getId(), couponId, mc.getExpiredAt(), mc.getCreatedAt()
                    );
                })
                .orElseGet(() -> createMemberCoupon(userId,coupon));

    }

    // 신규 쿠폰 발급 로직
    private CouponIssuanceResult createMemberCoupon(Long userId, Coupon coupon) {
        // 발급 수량 증가 (재고 차감 효과: totalQuantity - issuedQuantity = 남은 재고)
        coupon.increaseIssuedQuantity();

        // 회원-쿠폰 엔티티 생성/저장
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);
        MemberCoupon saved = memberCouponRepository.save(memberCoupon);

        log.info("쿠폰 발급 완료 - userId: {}, couponId: {}, 남은 재고: {}",
                userId, coupon.getId(),
                coupon.getRemainingQuantity() != null ?
                    coupon.getRemainingQuantity() : "무제한");

        return CouponIssuanceResult.issued(
                saved.getId(),
                coupon.getId(),
                saved.getExpiredAt(),
                saved.getCreatedAt()
        );
    }


    // 결과 DTO ? => 따로 빼야 하나 ? => 서비스 내부에서만 쓰임
    public record CouponIssuanceResult(Long memberCouponId,
                                       Long couponId,
                                       LocalDateTime expiredAt,
                                       LocalDateTime issuedAt,
                                       boolean duplicate) {
        public static CouponIssuanceResult issued(Long memberCouponId,
                                               Long couponId,
                                               LocalDateTime expiredAt,
                                               LocalDateTime issuedAt) {
            return new CouponIssuanceResult(memberCouponId, couponId, expiredAt, issuedAt,false);
        }
        public static CouponIssuanceResult duplicate(Long memberCouponId,
                                                      Long couponId,
                                                      LocalDateTime expiredAt,
                                                      LocalDateTime issuedAt) {
            return new CouponIssuanceResult(memberCouponId, couponId, expiredAt, issuedAt, true);
        }
    }
}

