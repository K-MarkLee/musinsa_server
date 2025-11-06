package com.mudosa.musinsa.common.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 금액 Value Object
 * - 불변 객체
 * - 도메인 로직 캡슐화
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    /**
     * 소수점 2자리
     */
    private static final int SCALE = 2;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private BigDecimal amount;

    public Money(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public Money(long amount) {
        this(BigDecimal.valueOf(amount));
    }

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    // 연산 메서드 (새 객체 반환 - 불변성)
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier));
    }

    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
        }

        BigDecimal result = this.amount.divide(divisor, SCALE + 10, ROUNDING_MODE);
        return new Money(result);
    }

    public Money divide(long divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    // 비교 메서드
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) <= 0;
    }

    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // 검증
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 음수일 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
