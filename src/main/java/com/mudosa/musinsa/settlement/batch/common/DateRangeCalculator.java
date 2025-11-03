package com.mudosa.musinsa.settlement.batch.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 정산 기간 계산 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateRangeCalculator {

    /* 어제 날짜 반환 (기준일: 오늘) */
    public static LocalDate getYesterday() {
        return getYesterday(LocalDate.now());
    }

    /* 어제 날짜 반환 (기준일 지정 가능) */
    public static LocalDate getYesterday(LocalDate baseDate) {
        return baseDate.minusDays(1);
    }

    /* 지난 주 월요일 반환 (기준일: 오늘) */
    public static LocalDate getLastWeekMonday() {
        return getLastWeekMonday(LocalDate.now());
    }

    /* 지난 주 월요일 반환 (기준일 지정 가능) */
    public static LocalDate getLastWeekMonday(LocalDate baseDate) {
        return baseDate.minusWeeks(1).with(DayOfWeek.MONDAY);
    }

    /* 지난 주 일요일 반환 (기준일: 오늘) */
    public static LocalDate getLastWeekSunday() {
        return getLastWeekSunday(LocalDate.now());
    }

    /* 지난 주 일요일 반환 (기준일 지정 가능) */
    public static LocalDate getLastWeekSunday(LocalDate baseDate) {
        return getLastWeekMonday(baseDate).plusDays(6);
    }

    /* 지난 달 시작일 반환 (기준일: 오늘) */
    public static LocalDate getLastMonthStart() {
        return getLastMonthStart(LocalDate.now());
    }

    /* 지난 달 시작일 반환 (기준일 지정 가능) */
    public static LocalDate getLastMonthStart(LocalDate baseDate) {
        YearMonth lastMonth = YearMonth.from(baseDate).minusMonths(1);
        return lastMonth.atDay(1);
    }

    /* 지난 달 종료일 반환 (기준일: 오늘) */
    public static LocalDate getLastMonthEnd() {
        return getLastMonthEnd(LocalDate.now());
    }

    /* 지난 달 종료일 반환 (기준일 지정 가능) */
    public static LocalDate getLastMonthEnd(LocalDate baseDate) {
        YearMonth lastMonth = YearMonth.from(baseDate).minusMonths(1);
        return lastMonth.atEndOfMonth();
    }

    /* 작년 연도 반환 (기준일: 오늘) */
    public static int getLastYear() {
        return getLastYear(LocalDate.now());
    }

    /* 작년 연도 반환 (기준일 지정 가능) */
    public static int getLastYear(LocalDate baseDate) {
        return baseDate.getYear() - 1;
    }
}