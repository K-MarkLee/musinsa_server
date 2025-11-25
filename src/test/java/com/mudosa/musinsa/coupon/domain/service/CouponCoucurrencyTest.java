package com.mudosa.musinsa.coupon.domain.service;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.coupon.domain.model.Coupon;
import com.mudosa.musinsa.coupon.domain.model.DiscountType;
import com.mudosa.musinsa.coupon.domain.repository.CouponRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 쿠폰 발급 동시성 테스트
 *
 * 목적: CouponIssuanceService의 비관적 락 메커니즘 검증
 * - PESSIMISTIC_WRITE 락이 제대로 작동하는가?
 * - 오버셀링(재고 초과 발급)이 발생하지 않는가?
 * - 멱등성이 보장되는가?
 */

@DisplayName("CouponIssuance 동시성 테스트")
class CouponCoucurrencyTest extends ServiceConfig {

    @Autowired
    private CouponIssuanceService couponIssuanceService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;


    // 테스트 1번
    @Test
    @DisplayName("[동시성] 100개 재고 쿠폰에 100명 동시 요청 - 정확히 100개만 발급")
    void concurrentIssuance_ExactStock_100Requests() throws InterruptedException {
        // given
        int THREAD_COUNT = 100;
        int COUPON_STOCK = 100;
        Long productId = 1L;

        Long couponId = createCouponInNewTransaction(COUPON_STOCK);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1; // 각 유저는 서로 다름
            executorService.submit(() -> {
                try {
                    couponIssuanceService.issueCoupon(userId, couponId, productId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("발급 실패 userId=" + userId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        verifyInNewTransaction(() -> {
            Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();

            System.out.println("===== 테스트 결과 =====");
            System.out.println("성공: " + successCount.get());
            System.out.println("실패: " + failCount.get());
            System.out.println("DB 발급 수량: " + updatedCoupon.getIssuedQuantity());
            System.out.println("======================");

            // 정확히 재고만큼만 발급되어야 함
            assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(COUPON_STOCK);
        });

        assertThat(successCount.get()).isEqualTo(COUPON_STOCK);
        assertThat(failCount.get()).isEqualTo(0);
    }

    // 테스트 2번
    @Test
    @DisplayName("[동시성] 100개 재고 쿠폰에 200명 동시 요청 - 100개만 발급, 100명 실패")
    void concurrentIssuance_ExactStock_200Requests() throws InterruptedException {
        // given
        int THREAD_COUNT = 200;
        int COUPON_STOCK = 100;
        Long productId = 1L;

        Long couponId = createCouponInNewTransaction(COUPON_STOCK);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssuanceService.issueCoupon(userId, couponId, productId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        verifyInNewTransaction(() -> {
            Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();
            assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(COUPON_STOCK);
        });

        // 정확히 재고만큼만 성공, 나머지는 실패
        assertThat(successCount.get()).isEqualTo(COUPON_STOCK);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - COUPON_STOCK);
    }

    // 테스트 3번
    @Test
    @DisplayName("[동시성] 같은 유저 10번 동시 요청 - 멱등성으로 1개만 발급")
    void concurrentIssuance_SameUser_Idempotent() throws InterruptedException {
        // given
        int THREAD_COUNT = 10;
        long SAME_USER_ID = 999L;
        Long productId = 1L;

        Long couponId = createCouponInNewTransaction(100);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successOrDuplicateCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    CouponIssuanceService.CouponIssuanceResult result =
                            couponIssuanceService.issueCoupon(SAME_USER_ID, couponId, productId);
                    successOrDuplicateCount.incrementAndGet();

                    if (result.duplicate()) {
                        System.out.println("중복 발급 감지");
                    }
                } catch (Exception e) {
                    System.err.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        verifyInNewTransaction(() -> {
            Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();

            // 같은 사용자는 1개만 발급되어야 함 (멱등성 보장)
            assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1);
        });

        // 모든 요청이 성공 또는 duplicate로 반환되어야 함
        assertThat(successOrDuplicateCount.get()).isEqualTo(THREAD_COUNT);
    }

    // 테스트 4번
    @Test
    @DisplayName("[동시성] 10개 재고에 50명 동시 요청 - 오버셀링 절대 발생 안함")
    void concurrentIssuance_NoOverselling() throws InterruptedException {
        // given
        int THREAD_COUNT = 50;
        int COUPON_STOCK = 10;
        Long productId = 1L;

        Long couponId = createCouponInNewTransaction(COUPON_STOCK);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssuanceService.issueCoupon(userId, couponId, productId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        verifyInNewTransaction(() -> {
            Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();

            System.out.println("===== 오버셀링 방지 테스트 =====");
            System.out.println("재고: " + COUPON_STOCK);
            System.out.println("성공: " + successCount.get());
            System.out.println("실패: " + failCount.get());
            System.out.println("DB 발급 수량: " + updatedCoupon.getIssuedQuantity());
            System.out.println("===============================");

            // 오버셀링이 절대 발생하면 안됨
            assertThat(updatedCoupon.getIssuedQuantity())
                    .isLessThanOrEqualTo(COUPON_STOCK)
                    .isEqualTo(successCount.get());
        });

        assertThat(successCount.get()).isLessThanOrEqualTo(COUPON_STOCK);
    }


    /*
    *
    * 테스트 헬퍼 메서드들
    *
    *  */


    /**
     * 새로운 트랜잭션에서 쿠폰 생성
     */

    private Long createCouponInNewTransaction(int totalQuantity) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return transactionTemplate.execute(status -> {
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(30);

            // 유니크 제약 조건 회피 : UUID 추가
            String uniqueCouponName = "동시성 테스트쿠폰_" + System.nanoTime();

            Coupon coupon = Coupon.create(
                    uniqueCouponName,
                    DiscountType.AMOUNT,
                    new BigDecimal("10000"),
                    startDate,
                    endDate,
                    totalQuantity
            );

            Coupon saved = couponRepository.save(coupon);
            entityManager.flush();
            entityManager.clear();

            return saved.getId();
        });
    }

    /**
     * 새로운 트랜잭션에서 검증
     */
    private void verifyInNewTransaction(Runnable verification) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transactionTemplate.execute(status -> {
            entityManager.clear();
            verification.run();
            return null;
        });
    }

    private void executeInNewTransaction(Runnable action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transactionTemplate.execute(status -> {
            action.run();
            return null;
        });
    }

}
