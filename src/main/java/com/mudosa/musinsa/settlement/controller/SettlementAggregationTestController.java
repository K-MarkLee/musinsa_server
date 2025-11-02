package com.mudosa.musinsa.settlement.controller;

import com.mudosa.musinsa.settlement.application.SettlementAggregationService;
import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import com.mudosa.musinsa.settlement.domain.model.SettlementMonthly;
import com.mudosa.musinsa.settlement.domain.model.SettlementWeekly;
import com.mudosa.musinsa.settlement.domain.model.SettlementYearly;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 정산 집계 테스트용 임시 Controller
 * TODO: 테스트 완료 후 삭제 또는 관리자 전용으로 변경
 */
@RestController
@RequestMapping("/api/test/settlement/aggregation")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class SettlementAggregationTestController {

    private final SettlementAggregationService aggregationService;

    /**
     * 일일 집계 실행
     *
     * GET /api/test/settlement/aggregation/daily?brandId=1&startDate=2025-10-01&endDate=2025-10-05
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> aggregateDaily(
        @RequestParam Long brandId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Daily aggregation request: brandId={}, startDate={}, endDate={}", brandId, startDate, endDate);

        try {
            List<SettlementDaily> result = aggregationService.aggregateToDaily(brandId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", result.size());
            response.put("data", result);
            response.put("message", "Daily aggregation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Daily aggregation failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 주간 집계 실행
     *
     * GET /api/test/settlement/aggregation/weekly?brandId=1&startDate=2025-10-01&endDate=2025-10-31
     */
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> aggregateWeekly(
        @RequestParam Long brandId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Weekly aggregation request: brandId={}, startDate={}, endDate={}", brandId, startDate, endDate);

        try {
            List<SettlementWeekly> result = aggregationService.aggregateToWeekly(brandId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", result.size());
            response.put("data", result);
            response.put("message", "Weekly aggregation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Weekly aggregation failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 월간 집계 실행
     *
     * GET /api/test/settlement/aggregation/monthly?brandId=1&startDate=2025-10-01&endDate=2025-10-31
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> aggregateMonthly(
        @RequestParam Long brandId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Monthly aggregation request: brandId={}, startDate={}, endDate={}", brandId, startDate, endDate);

        try {
            List<SettlementMonthly> result = aggregationService.aggregateToMonthly(brandId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", result.size());
            response.put("data", result);
            response.put("message", "Monthly aggregation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Monthly aggregation failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 연간 집계 실행
     *
     * GET /api/test/settlement/aggregation/yearly?brandId=1&year=2025
     */
    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> aggregateYearly(
        @RequestParam Long brandId,
        @RequestParam int year
    ) {
        log.info("Yearly aggregation request: brandId={}, year={}", brandId, year);

        try {
            SettlementYearly result = aggregationService.aggregateToYearly(brandId, year);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Yearly aggregation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Yearly aggregation failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 전체 집계 실행 (Daily + Monthly)
     *
     * POST /api/test/settlement/aggregation/all?brandId=1&startDate=2025-10-01&endDate=2025-10-31
     */
    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> aggregateAll(
        @RequestParam Long brandId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Full aggregation request: brandId={}, startDate={}, endDate={}", brandId, startDate, endDate);

        try {
            // 1. Daily 집계
            List<SettlementDaily> dailyResults = aggregationService.aggregateToDaily(brandId, startDate, endDate);

            // 2. Monthly 집계
            List<SettlementMonthly> monthlyResults = aggregationService.aggregateToMonthly(brandId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("daily", Map.of("count", dailyResults.size(), "data", dailyResults));
            response.put("monthly", Map.of("count", monthlyResults.size(), "data", monthlyResults));
            response.put("message", "Full aggregation completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Full aggregation failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}