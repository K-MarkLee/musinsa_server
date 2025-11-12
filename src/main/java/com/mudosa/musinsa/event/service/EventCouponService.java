package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.coupon.domain.model.Coupon;
import com.mudosa.musinsa.coupon.domain.service.CouponIssuanceService;
import com.mudosa.musinsa.event.model.Event;

import com.mudosa.musinsa.event.model.EventOption;

import com.mudosa.musinsa.event.repository.EventOptionRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.model.ProductOption;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j

public class EventCouponService {

    private final EventService eventService;
    private final EventOptionRepository eventOptionRepository;
    private final EventEntryService eventEntryService;
    private final CouponIssuanceService couponIssuanceService;


    /*

    * 목적: 동시성 제어/슬롯 확보. 토큰이 close 될 때(블록 종료) 슬롯 반납.
    * 혼잡 시 진입 제한/큐 처리/TTL 관리 등을 캡슐화

    * */

    // 이벤트 쿠폰 발급 => 쿠폰 발급 버튼을 눌렀을 때 실행
    @Transactional
    public EventCouponIssueResult issueCoupon(Long eventId,
                                              Long eventOptionId,
                                              Long userId ){

        // 1. 동시성 제어 : 같은 유저의 중복 요청 방지
        try (EventEntryService.EventEntryToken ignored = eventEntryService.acquireSlot(eventId, userId)){

            // 2. 이벤트 옵션 조회 ( 비관적 락 )
            EventOption eventOption = eventOptionRepository.findByEventIdAndIdForUpdate(eventId,eventOptionId)
                    // 레포에 생성 필요 -1
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

            Event event = eventOption.getEvent();

            // 3. 이벤트 상태 검증
            validateEventState(event);

            // 4. 쿠폰 정보 확인
            Coupon coupon = Optional.ofNullable(event.getCoupon())
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_COUPON_NOT_ASSIGNED));




            //  5. 멱등성 체크 : 이미 발급된 쿠폰이 있는지 먼저 조회 , 있으면 그대로 재사용

            Long couponId = coupon.getId();

            Optional<CouponIssuanceService.CouponIssuanceResult> existing = couponIssuanceService
                    .findIssuedCoupon(userId, couponId);
            if (existing.isPresent()) {
                log.info("멱등 처리 - eventId: {}, userId: {}", eventId, userId);
                return EventCouponIssueResult.from(existing.get());
            }

            // 사용자별 제한 확인
            validateUserLimit(event, coupon, userId);

//            // 재고 확인
//            ensureEventStockAvailable(eventOption);

            // 상품 ID 확인
            Long productId = resolveProductId(eventOption);

            // 6. 실제 쿠폰 발급 ! => 쿠폰 도메인
            CouponIssuanceService.CouponIssuanceResult issuanceResult = couponIssuanceService
                    .issueCoupon(userId, couponId, productId);


            // 10. 중복 발급 감지
            if(issuanceResult.duplicate()){
                log.info("중복 발급 감지 - eventId: {}, userId: {}", eventId, userId);
                return EventCouponIssueResult.from(issuanceResult);
            }


            decreaseEventStock(eventOption); // 기존 재고 차감 호출 (event => inventory)

            return EventCouponIssueResult.from(issuanceResult);
        }
    }

    // 현재 진행하고 있는 유효한 이벤트인지
    private void validateEventState(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if (!event.isOngoing(now)) {
            throw new BusinessException(ErrorCode.EVENT_NOT_OPEN);
        }
    }

    // 이벤트 발급 이력 테이블 (X) => MemberCoupon 테이블을 기준으로 발급 추적
    // 한 이벤트 옵션 기준으로 쿠폰을 사용한다는 전제로 사용자 검증
    private void validateUserLimit(Event event, Coupon coupon, Long userId) {

        long issuedCount = couponIssuanceService.countIssuedByUser(userId,coupon.getId()); // couponIssuance 서비스에 새로생성필요
        if (issuedCount >= event.getLimitPerUser()) {
            throw new BusinessException(ErrorCode.EVENT_USER_LIMIT_EXCEEDED);
        }
    }

    // 기존 상품옵션 단위 재고 차감
    private void decreaseEventStock(EventOption eventOption) {
        try{
            eventOption.decreaseStock(1);
        }catch (IllegalStateException exception){
            throw new BusinessException(ErrorCode.EVENT_STOCK_EMPTY, exception.getMessage());
        }
    }

//    // 재고가 있는 이벤트 옵션인지 => 필요할까 ? 어짜피 쿠폰 발급인데
//    private void ensureEventStockAvailable(EventOption eventOption) {
//        if(eventOption.getEventStock() == null || eventOption.getEventStock() <= 0) {
//            throw new BusinessException(ErrorCode.EVENT_STOCK_EMPTY);
//        }
//    }

    // 이벤트에(쿠폰) 매핑된 상품 ID
    private Long resolveProductId(EventOption eventOption) {
        ProductOption productOption = eventOption.getProductOption();
        if(productOption == null || productOption.getProduct() == null) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
        return productOption.getProduct().getProductId();
    }

    public record EventCouponIssueResult(Long memberCouponId,
                                        Long couponId,
                                        LocalDateTime issuedAt,
                                        LocalDateTime expiredAt,
                                        boolean duplicate ) {
        private static EventCouponIssueResult from(CouponIssuanceService.CouponIssuanceResult result){
            return new EventCouponIssueResult(
                    result.memberCouponId(),
                    result.couponId(),
                    result.issuedAt(),
                    result.expiredAt(),
                    result.duplicate()
            );
        }
    }
}
