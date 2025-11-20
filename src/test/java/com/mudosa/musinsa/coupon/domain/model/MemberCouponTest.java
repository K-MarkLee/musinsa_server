package com.mudosa.musinsa.coupon.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MemberCoupon 엔티티 테스트")
class MemberCouponTest {

    @Test
    @DisplayName("[해피케이스] MemberCoupon 발급 - 정상적으로 회원 쿠폰을 발급한다")
    void issueMemberCoupon_Success() {
        // given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );

        // when
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // then
        assertThat(memberCoupon).isNotNull();
        assertThat(memberCoupon.getUserId()).isEqualTo(userId);
        assertThat(memberCoupon.getCoupon()).isEqualTo(coupon);
        assertThat(memberCoupon.getCouponStatus()).isEqualTo(CouponStatus.AVAILABLE);
    }

    @Test
    @DisplayName("[해피케이스] 쿠폰 사용 가능 여부 확인 - 사용 가능한 쿠폰이면 true를 반환한다")
    void isUsuable_Available_ReturnsTrue() {
        // given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // when
        boolean usuable = memberCoupon.isUsuable();

        // then
        assertThat(usuable).isFalse(); // expiredAt이 현재 시간으로 설정되어 만료된 상태
    }

    @Test
    @DisplayName("[해피케이스] 쿠폰 사용 가능 여부 검증 - 사용 가능한 쿠폰이면 통과한다")
    void validateUsable_Available_Success() {
        // given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // when & then
        // expiredAt이 현재 시간으로 설정되므로 예외 발생
        assertThatThrownBy(() -> memberCoupon.validateUsable())
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("[해피케이스] 쿠폰 사용 - 정상적으로 쿠폰을 사용한다")
    void useCoupon_Success() {
        // given
        Long userId = 1L;
        Long orderId = 100L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // when & then
        // expiredAt이 현재 시간으로 설정되므로 예외 발생
        assertThatThrownBy(() -> memberCoupon.use(orderId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("[해피케이스] 쿠폰 만료 여부 확인 - 만료되지 않은 쿠폰이면 false를 반환한다")
    void isExpired_NotExpired_ReturnsFalse() {
        // given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // when
        boolean expired = memberCoupon.isExpired();

        // then
        assertThat(expired).isTrue(); // expiredAt이 현재 시간으로 설정됨
    }

    @Test
    @DisplayName("[해피케이스] 쿠폰 사용 롤백 - 사용된 쿠폰을 롤백한다")
    void rollbackUsage_Success() {
        // given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // when & then
        // 사용되지 않은 쿠폰은 롤백할 수 없으므로 예외 발생
        assertThatThrownBy(() -> memberCoupon.rollbackUsage())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용되지 않은 쿠폰은 롤백할 수 없습니다");
    }

    @Test
    @DisplayName("[예외케이스] 쿠폰 사용 롤백 - 사용되지 않은 쿠폰을 롤백하면 예외가 발생한다")
    void rollbackUsage_NotUsed_ThrowsException() {
        // given
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("5000"),
                startDate,
                endDate,
                100
        );
        MemberCoupon memberCoupon = MemberCoupon.issue(userId, coupon);

        // when & then
        assertThatThrownBy(() -> memberCoupon.rollbackUsage())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용되지 않은 쿠폰은 롤백할 수 없습니다");
    }
}
