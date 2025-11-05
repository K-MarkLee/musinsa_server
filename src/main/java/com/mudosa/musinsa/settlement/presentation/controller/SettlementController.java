package com.mudosa.musinsa.settlement.presentation.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.settlement.application.SettlementQueryService;
import com.mudosa.musinsa.settlement.presentation.dto.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 정산 조회 API
 *
 * 프론트엔드에 정산 데이터 조회 API를 제공
 * 프론트엔드에서 API 호출 시 실행
 *
 * API 목록
 * - GET /api/settlements/daily: 일일 정산 목록 조회
 * - GET /api/settlements/daily/{id}: 일일 정산 상세 조회
 * - GET /api/settlements/weekly: 주간 정산 목록 조회
 * - GET /api/settlements/monthly: 월간 정산 목록 조회
 * - GET /api/settlements/yearly: 연간 정산 목록 조회
 * - GET /api/settlements/statistics: 정산 통계 조회
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DAILY_SORT_FIELD = "settlementDate";
    private static final String WEEKLY_SORT_FIELD = "weekStartDate";
    private static final String MONTHLY_SORT_FIELD = "settlementYear";
    private static final String YEARLY_SORT_FIELD = "settlementYear";

    private final SettlementQueryService settlementQueryService;

    /* 일일 정산 목록 조회 */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<Page<SettlementDailyResponse>>> getDailySettlements(
        @RequestParam(required = false) Long brandId,
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = DAILY_SORT_FIELD, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("일일 정산 목록 조회 요청 - brandId: {}, page: {}, size: {}",
            brandId, pageable.getPageNumber(), pageable.getPageSize());

        Page<SettlementDailyResponse> data = settlementQueryService.getDailySettlements(brandId, pageable);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /* 일일 정산 상세 조회 */
    @GetMapping("/daily/{settlementDailyId}")
    public ResponseEntity<SettlementDailyResponse> getDailySettlement(
        @PathVariable @Min(1) Long settlementDailyId
    ) {
        log.debug("일일 정산 상세 조회 요청 - id: {}", settlementDailyId);

        return ResponseEntity.ok(settlementQueryService.getDailySettlement(settlementDailyId));
    }

    /* 주간 정산 목록 조회 */
    @GetMapping("/weekly")
    public ResponseEntity<Page<SettlementWeeklyResponse>> getWeeklySettlements(
        @RequestParam @Min(1) Long brandId,
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = WEEKLY_SORT_FIELD, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("주간 정산 목록 조회 요청 - brandId: {}, page: {}, size: {}",
            brandId, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(settlementQueryService.getWeeklySettlements(brandId, pageable));
    }

    /* 주간 정산 상세 조회 */
    @GetMapping("/weekly/{settlementWeeklyId}")
    public ResponseEntity<SettlementWeeklyResponse> getWeeklySettlement(
        @PathVariable @Min(1) Long settlementWeeklyId
    ) {
        log.debug("주간 정산 상세 조회 요청 - id: {}", settlementWeeklyId);

        return ResponseEntity.ok(settlementQueryService.getWeeklySettlement(settlementWeeklyId));
    }

    /* 월간 정산 목록 조회 */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Page<SettlementMonthlyResponse>>> getMonthlySettlements(
        @RequestParam(required = false) Long brandId,
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = MONTHLY_SORT_FIELD, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("월간 정산 목록 조회 요청 - brandId: {}, page: {}, size: {}",
            brandId, pageable.getPageNumber(), pageable.getPageSize());

        Page<SettlementMonthlyResponse> data = settlementQueryService.getMonthlySettlements(brandId, pageable);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /* 월간 정산 상세 조회 */
    @GetMapping("/monthly/{settlementMonthlyId}")
    public ResponseEntity<SettlementMonthlyResponse> getMonthlySettlement(
        @PathVariable @Min(1) Long settlementMonthlyId
    ) {
        log.debug("월간 정산 상세 조회 요청 - id: {}", settlementMonthlyId);

        return ResponseEntity.ok(settlementQueryService.getMonthlySettlement(settlementMonthlyId));
    }

    /* 연간 정산 목록 조회 */
    @GetMapping("/yearly")
    public ResponseEntity<Page<SettlementYearlyResponse>> getYearlySettlements(
        @RequestParam @Min(1) Long brandId,
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = YEARLY_SORT_FIELD, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("연간 정산 목록 조회 요청 - brandId: {}, page: {}, size: {}",
            brandId, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(settlementQueryService.getYearlySettlements(brandId, pageable));
    }

    /* 연간 정산 상세 조회 */
    @GetMapping("/yearly/{settlementYearlyId}")
    public ResponseEntity<SettlementYearlyResponse> getYearlySettlement(
        @PathVariable @Min(1) Long settlementYearlyId
    ) {
        log.debug("연간 정산 상세 조회 요청 - id: {}", settlementYearlyId);

        return ResponseEntity.ok(settlementQueryService.getYearlySettlement(settlementYearlyId));
    }

    /* 거래별 정산 목록 조회 */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<SettlementPerTransactionResponse>>> getPerTransactionSettlements(
        @RequestParam @Min(1) Long brandId,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("거래별 정산 목록 조회 요청 - brandId: {}, startDate: {}, endDate: {}, page: {}, size: {}",
            brandId, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

        Page<SettlementPerTransactionResponse> data = settlementQueryService.getPerTransactionSettlements(
            brandId, startDate, endDate, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /* 거래별 정산 단건 조회 */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<SettlementPerTransactionResponse> getPerTransactionSettlement(
        @PathVariable @Min(1) Long transactionId
    ) {
        log.debug("거래별 정산 단건 조회 요청 - id: {}", transactionId);

        return ResponseEntity.ok(settlementQueryService.getPerTransactionSettlement(transactionId));
    }

    /* 정산 통계 조회 - 오늘/이번주/이번달/올해/전체 통계 (brandId 없으면 전체 브랜드 합계) */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<SettlementStatisticsResponse>> getStatistics(
        @RequestParam(required = false) Long brandId
    ) {
        log.debug("정산 통계 조회 요청 - brandId: {}", brandId);

        SettlementStatisticsResponse data = settlementQueryService.getStatistics(brandId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}