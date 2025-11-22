package com.mudosa.musinsa.common.vo;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Money {

    private static final int SCALE = 2;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private BigDecimal amount;

    public static final Money ZERO = new Money(0);

    public Money(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money of(BigDecimal amount){return new Money(amount);}
    public static Money of(Long amount){return new Money(BigDecimal.valueOf(amount));}

    public Money(long amount) {
        this(BigDecimal.valueOf(amount));
    }

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

        BigDecimal result = this.amount.divide(divisor, SCALE, ROUNDING_MODE);
        return new Money(result);
    }

    public Money divide(long divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    public boolean isLessThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) <= 0;
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
}
