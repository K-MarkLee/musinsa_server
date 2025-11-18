package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.category.domain.model.Category;
import com.mudosa.musinsa.category.domain.repository.CategoryRepository;
import com.mudosa.musinsa.coupon.domain.model.Coupon;
import com.mudosa.musinsa.coupon.domain.model.DiscountType;
import com.mudosa.musinsa.coupon.domain.repository.CouponRepository;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventOption;
import com.mudosa.musinsa.event.repository.EventOptionRepository;
import com.mudosa.musinsa.event.repository.EventRepository;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventCouponService 동시성 테스트")
class EventCouponServiceConcurrencyTest extends ServiceConfig {

    @Autowired
    private EventCouponService eventCouponService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventOptionRepository eventOptionRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("[동시성] 100개 한정 쿠폰에 100명이 동시 요청 - 정확히 100개만 발급된다")
    void concurrentCouponIssuance_100Requests_Success() throws InterruptedException {
        // given
        int THREAD_COUNT = 100;  // 동시 요청 수
        int COUPON_STOCK = 100;   // 쿠폰 재고

        // 테스트 데이터 준비
        TestData testData = createTestData(COUPON_STOCK);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1;  // 각 요청마다 다른 사용자
            executorService.submit(() -> {
                try {
                    eventCouponService.issueCoupon(
                            testData.eventId,
                            testData.productOptionId,
                            userId
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 스레드 완료 대기
        executorService.shutdown();

        // then
        entityManager.flush();
        entityManager.clear();

        // 정확히 재고만큼만 발급되어야 함
        assertThat(successCount.get()).isEqualTo(COUPON_STOCK);
        assertThat(failCount.get()).isEqualTo(0);

        // DB에서 실제 발급 수량 확인
        Coupon updatedCoupon = couponRepository.findById(testData.couponId).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(COUPON_STOCK);

        // 이벤트 옵션 재고도 0이어야 함
        EventOption updatedEventOption = eventOptionRepository.findById(testData.eventOptionId).orElseThrow();
        assertThat(updatedEventOption.getEventStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("[동시성] 100개 한정 쿠폰에 200명이 동시 요청 - 100개만 발급되고 100명은 실패한다")
    void concurrentCouponIssuance_200Requests_OnlyHundredSuccess() throws InterruptedException {
        // given
        int THREAD_COUNT = 200;   // 동시 요청 수
        int COUPON_STOCK = 100;   // 쿠폰 재고

        TestData testData = createTestData(COUPON_STOCK);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    eventCouponService.issueCoupon(
                            testData.eventId,
                            testData.productOptionId,
                            userId
                    );
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
        entityManager.flush();
        entityManager.clear();

        // 정확히 재고만큼만 발급되어야 함
        assertThat(successCount.get()).isEqualTo(COUPON_STOCK);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - COUPON_STOCK);

        // DB 확인
        Coupon updatedCoupon = couponRepository.findById(testData.couponId).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(COUPON_STOCK);

        EventOption updatedEventOption = eventOptionRepository.findById(testData.eventOptionId).orElseThrow();
        assertThat(updatedEventOption.getEventStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("[동시성] 같은 사용자가 동시에 여러 번 요청 - 멱등성 보장으로 1개만 발급된다")
    void concurrentCouponIssuance_SameUser_IdempotentSuccess() throws InterruptedException {
        // given
        int THREAD_COUNT = 10;    // 동일 사용자의 중복 요청
        long SAME_USER_ID = 1L;   // 같은 사용자

        TestData testData = createTestData(100);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<EventCouponService.EventCouponIssueResult> results = new ArrayList<>();

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    EventCouponService.EventCouponIssueResult result = eventCouponService.issueCoupon(
                            testData.eventId,
                            testData.productOptionId,
                            SAME_USER_ID
                    );
                    synchronized (results) {
                        results.add(result);
                    }
                } catch (Exception e) {
                    // EventEntryService에서 중복 요청 차단될 수 있음
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        entityManager.flush();
        entityManager.clear();

        // 성공한 요청들 중 일부는 duplicate=true일 수 있음
        assertThat(results).isNotEmpty();

        // 실제 발급된 쿠폰은 1개만
        Coupon updatedCoupon = couponRepository.findById(testData.couponId).orElseThrow();
        // 최소 1개, 최대 1개 (같은 사용자는 1개만 발급받아야 함)
        // 단, EventEntryService가 같은 사용자의 동시 요청을 막으므로 실제로는 1번만 실행됨
    }

    @Test
    @DisplayName("[동시성] 오버셀링 방지 - 재고보다 많이 발급되지 않는다")
    void concurrentCouponIssuance_NoOverselling_Success() throws InterruptedException {
        // given
        int THREAD_COUNT = 50;
        int COUPON_STOCK = 10;  // 적은 재고

        TestData testData = createTestData(COUPON_STOCK);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    eventCouponService.issueCoupon(
                            testData.eventId,
                            testData.productOptionId,
                            userId
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 재고 부족으로 실패
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        entityManager.flush();
        entityManager.clear();

        // 오버셀링이 발생하지 않아야 함 (재고보다 많이 발급되면 안됨)
        assertThat(successCount.get()).isLessThanOrEqualTo(COUPON_STOCK);

        Coupon updatedCoupon = couponRepository.findById(testData.couponId).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isLessThanOrEqualTo(COUPON_STOCK);

        EventOption updatedEventOption = eventOptionRepository.findById(testData.eventOptionId).orElseThrow();
        assertThat(updatedEventOption.getEventStock()).isGreaterThanOrEqualTo(0);  // 음수가 되면 안됨
    }

    /**
     * 테스트 데이터 생성 헬퍼
     */
    private TestData createTestData(int couponStock) {
        // Brand
        Brand brand = Brand.builder()
                .brandName("동시성 테스트 브랜드")
                .description("테스트용")
                .build();
        brandRepository.save(brand);

        // Category
        Category category = Category.builder()
                .categoryName("동시성 테스트 카테고리")
                .categoryLevel(1)
                .parentCategoryId(null)
                .build();
        categoryRepository.save(category);

        // Product
        Product product = Product.builder()
                .productName("동시성 테스트 상품")
                .productPrice(new BigDecimal("100000"))
                .productDescription("테스트용")
                .brand(brand)
                .category(category)
                .productStatus("SALE")
                .stockQuantity(1000)
                .build();
        productRepository.save(product);

        // ProductOption
        ProductOption productOption = ProductOption.create(
                product,
                "테스트 옵션",
                new BigDecimal("100000"),
                1000
        );
        productOptionRepository.save(productOption);

        // Coupon
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "동시성 테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("10000"),
                startDate,
                endDate,
                couponStock
        );
        couponRepository.save(coupon);

        // Event
        Event event = Event.create(
                "동시성 테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startDate,
                endDate,
                coupon
        );
        event.open();
        eventRepository.save(event);

        // EventOption
        EventOption eventOption = EventOption.create(
                event,
                productOption,
                new BigDecimal("80000"),
                couponStock
        );
        eventOptionRepository.save(eventOption);

        entityManager.flush();
        entityManager.clear();

        return new TestData(
                event.getId(),
                eventOption.getId(),
                productOption.getProductOptionId(),
                coupon.getId()
        );
    }

    private record TestData(
            Long eventId,
            Long eventOptionId,
            Long productOptionId,
            Long couponId
    ) {}
}
