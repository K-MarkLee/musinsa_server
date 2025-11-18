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
import com.mudosa.musinsa.exception.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventCouponService 테스트")
class EventCouponServiceTest extends ServiceConfig {

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
    @DisplayName("[해피케이스] 쿠폰 발급 - 정상적으로 이벤트 쿠폰을 발급한다")
    void issueCoupon_Success() {
        // given
        Long userId = 1L;

        // Brand 생성
        Brand brand = Brand.builder()
                .brandName("테스트 브랜드")
                .description("테스트 브랜드 설명")
                .build();
        brandRepository.save(brand);

        // Category 생성
        Category category = Category.builder()
                .categoryName("테스트 카테고리")
                .categoryLevel(1)
                .parentCategoryId(null)
                .build();
        categoryRepository.save(category);

        // Product 생성
        Product product = Product.builder()
                .productName("테스트 상품")
                .productPrice(new BigDecimal("100000"))
                .productDescription("테스트 상품 설명")
                .brand(brand)
                .category(category)
                .productStatus("SALE")
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // ProductOption 생성
        ProductOption productOption = ProductOption.create(
                product,
                "테스트 옵션",
                new BigDecimal("100000"),
                50
        );
        productOptionRepository.save(productOption);

        // Coupon 생성
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "이벤트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("10000"),
                startDate,
                endDate,
                100
        );
        couponRepository.save(coupon);

        // Event 생성
        Event event = Event.create(
                "테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startDate,
                endDate,
                coupon
        );
        event.open(); // 이벤트를 OPEN 상태로 변경
        eventRepository.save(event);

        // EventOption 생성
        EventOption eventOption = EventOption.create(
                event,
                productOption,
                new BigDecimal("80000"),
                50
        );
        eventOptionRepository.save(eventOption);

        entityManager.flush();
        entityManager.clear();

        // when
        EventCouponService.EventCouponIssueResult result = eventCouponService.issueCoupon(
                event.getId(),
                productOption.getProductOptionId(),
                userId
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.memberCouponId()).isNotNull();
        assertThat(result.couponId()).isEqualTo(coupon.getId());
        assertThat(result.duplicate()).isFalse();
    }

    @Test
    @DisplayName("[해피케이스] 쿠폰 중복 발급 - 이미 발급받은 쿠폰은 중복 처리된다")
    void issueCoupon_Duplicate_Success() {
        // given
        Long userId = 2L;

        // Brand 생성
        Brand brand = Brand.builder()
                .brandName("테스트 브랜드2")
                .description("테스트 브랜드 설명2")
                .build();
        brandRepository.save(brand);

        // Category 생성
        Category category = Category.builder()
                .categoryName("테스트 카테고리2")
                .categoryLevel(1)
                .parentCategoryId(null)
                .build();
        categoryRepository.save(category);

        // Product 생성
        Product product = Product.builder()
                .productName("테스트 상품2")
                .productPrice(new BigDecimal("100000"))
                .productDescription("테스트 상품 설명2")
                .brand(brand)
                .category(category)
                .productStatus("SALE")
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // ProductOption 생성
        ProductOption productOption = ProductOption.create(
                product,
                "테스트 옵션2",
                new BigDecimal("100000"),
                50
        );
        productOptionRepository.save(productOption);

        // Coupon 생성
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "중복 테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("10000"),
                startDate,
                endDate,
                100
        );
        couponRepository.save(coupon);

        // Event 생성
        Event event = Event.create(
                "중복 테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                2, // 2번까지 발급 가능
                true,
                startDate,
                endDate,
                coupon
        );
        event.open();
        eventRepository.save(event);

        // EventOption 생성
        EventOption eventOption = EventOption.create(
                event,
                productOption,
                new BigDecimal("80000"),
                50
        );
        eventOptionRepository.save(eventOption);

        entityManager.flush();
        entityManager.clear();

        // 첫 번째 발급
        eventCouponService.issueCoupon(event.getId(), productOption.getProductOptionId(), userId);

        // when
        // 두 번째 발급 시도
        EventCouponService.EventCouponIssueResult result = eventCouponService.issueCoupon(
                event.getId(),
                productOption.getProductOptionId(),
                userId
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.duplicate()).isTrue();
    }

    @Test
    @DisplayName("[예외케이스] 쿠폰 발급 - 존재하지 않는 이벤트 옵션이면 예외가 발생한다")
    void issueCoupon_EventOptionNotFound_ThrowsException() {
        // given
        Long userId = 3L;
        Long nonExistentEventId = 999999L;
        Long nonExistentProductOptionId = 999999L;

        // when & then
        assertThatThrownBy(() -> eventCouponService.issueCoupon(
                nonExistentEventId,
                nonExistentProductOptionId,
                userId
        ))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("[예외케이스] 쿠폰 발급 - 이벤트가 OPEN 상태가 아니면 예외가 발생한다")
    void issueCoupon_EventNotOpen_ThrowsException() {
        // given
        Long userId = 4L;

        // Brand 생성
        Brand brand = Brand.builder()
                .brandName("테스트 브랜드3")
                .description("테스트 브랜드 설명3")
                .build();
        brandRepository.save(brand);

        // Category 생성
        Category category = Category.builder()
                .categoryName("테스트 카테고리3")
                .categoryLevel(1)
                .parentCategoryId(null)
                .build();
        categoryRepository.save(category);

        // Product 생성
        Product product = Product.builder()
                .productName("테스트 상품3")
                .productPrice(new BigDecimal("100000"))
                .productDescription("테스트 상품 설명3")
                .brand(brand)
                .category(category)
                .productStatus("SALE")
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // ProductOption 생성
        ProductOption productOption = ProductOption.create(
                product,
                "테스트 옵션3",
                new BigDecimal("100000"),
                50
        );
        productOptionRepository.save(productOption);

        // Coupon 생성
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "테스트 쿠폰3",
                DiscountType.AMOUNT,
                new BigDecimal("10000"),
                startDate,
                endDate,
                100
        );
        couponRepository.save(coupon);

        // Event 생성 (DRAFT 상태로 유지)
        Event event = Event.create(
                "DRAFT 상태 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                1,
                true,
                startDate,
                endDate,
                coupon
        );
        // open() 호출하지 않음 - DRAFT 상태 유지
        eventRepository.save(event);

        // EventOption 생성
        EventOption eventOption = EventOption.create(
                event,
                productOption,
                new BigDecimal("80000"),
                50
        );
        eventOptionRepository.save(eventOption);

        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> eventCouponService.issueCoupon(
                event.getId(),
                productOption.getProductOptionId(),
                userId
        ))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("[예외케이스] 쿠폰 발급 - 재고가 부족하면 예외가 발생한다")
    void issueCoupon_InsufficientStock_ThrowsException() {
        // given
        Long userId1 = 5L;
        Long userId2 = 6L;

        // Brand 생성
        Brand brand = Brand.builder()
                .brandName("테스트 브랜드4")
                .description("테스트 브랜드 설명4")
                .build();
        brandRepository.save(brand);

        // Category 생성
        Category category = Category.builder()
                .categoryName("테스트 카테고리4")
                .categoryLevel(1)
                .parentCategoryId(null)
                .build();
        categoryRepository.save(category);

        // Product 생성
        Product product = Product.builder()
                .productName("테스트 상품4")
                .productPrice(new BigDecimal("100000"))
                .productDescription("테스트 상품 설명4")
                .brand(brand)
                .category(category)
                .productStatus("SALE")
                .stockQuantity(100)
                .build();
        productRepository.save(product);

        // ProductOption 생성
        ProductOption productOption = ProductOption.create(
                product,
                "테스트 옵션4",
                new BigDecimal("100000"),
                50
        );
        productOptionRepository.save(productOption);

        // Coupon 생성
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        Coupon coupon = Coupon.create(
                "재고 부족 테스트 쿠폰",
                DiscountType.AMOUNT,
                new BigDecimal("10000"),
                startDate,
                endDate,
                100
        );
        couponRepository.save(coupon);

        // Event 생성
        Event event = Event.create(
                "재고 부족 테스트 이벤트",
                "설명",
                Event.EventType.DROP,
                Event.LimitScope.EVENT,
                10,
                true,
                startDate,
                endDate,
                coupon
        );
        event.open();
        eventRepository.save(event);

        // EventOption 생성 (재고 1개만)
        EventOption eventOption = EventOption.create(
                event,
                productOption,
                new BigDecimal("80000"),
                1 // 재고 1개
        );
        eventOptionRepository.save(eventOption);

        entityManager.flush();
        entityManager.clear();

        // 첫 번째 사용자가 발급받음
        eventCouponService.issueCoupon(event.getId(), productOption.getProductOptionId(), userId1);

        // when & then
        // 두 번째 사용자가 발급 시도 - 재고 부족으로 실패
        assertThatThrownBy(() -> eventCouponService.issueCoupon(
                event.getId(),
                productOption.getProductOptionId(),
                userId2
        ))
                .isInstanceOf(BusinessException.class);
    }
}
