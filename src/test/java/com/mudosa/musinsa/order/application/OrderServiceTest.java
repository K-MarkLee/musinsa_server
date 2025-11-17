package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import com.mudosa.musinsa.order.application.dto.OrderCreateItem;
import com.mudosa.musinsa.order.application.dto.OrderCreateRequest;
import com.mudosa.musinsa.order.application.dto.OrderCreateResponse;
import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderServiceTest extends ServiceConfig {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    void tearDown() {

    }

    @DisplayName("상품 옵션과 수량을 갖는 맵으로 주문을 생성한다.")
    @Test
    void createOrder(){
        //given
        Brand brand = createBrand();
        em.persist(brand);

        Product product = createProduct(brand, true);
        em.persist(product);

        Inventory inventory = createInventory(10);
        em.persist(inventory);

        ProductOption productOption = createProductOption(product, inventory, 10000L);
        em.persist(productOption);

        User user = createUser();
        em.persist(user);

        em.flush();
        em.clear();

        OrderCreateRequest request = getOrderCreateRequest(productOption.getProductOptionId(), 2);

        //when
        OrderCreateResponse response = orderService.createPendingOrder(request, user.getId());

        //then
        Order order = orderRepository.findById(response.getOrderId()).orElseThrow(null);

        assertThat(order)
                .extracting("orderNo", "userId")
                .contains(response.getOrderNo(), user.getId());

        assertThat(order.getOrderProducts())
                .extracting("order")
                .containsExactly(order);
    }


    @DisplayName("주문 상품 옵션의 id가 올바르지 않을때 예외를 발생한다.")
    @Test
    void createOrderWithInvalidProductOptionId(){
        //given
        Brand brand = createBrand();
        em.persist(brand);

        Product product = createProduct(brand, true);
        em.persist(product);

        Inventory inventory = createInventory(10);
        em.persist(inventory);

        ProductOption productOption = createProductOption(product, inventory, 10000L);
        em.persist(productOption);

        User user = createUser();
        em.persist(user);

        em.flush();
        em.clear();

        OrderCreateRequest request = getOrderCreateRequest(-10L, 2);

        //when & then
        assertThatThrownBy(()->orderService.createPendingOrder(request, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("상품 옵션을 찾을 수 없습니다");
    }

    @DisplayName("주문 상품이 유효하지 않은 상품을 주문할 때 예외를 발생한다.")
    @Test
    void createOrderWithInvalidProduct(){
        //given
        Brand brand = createBrand();
        em.persist(brand);

        Product product = createProduct(brand, false);
        em.persist(product);

        Inventory inventory = createInventory(10);
        em.persist(inventory);

        ProductOption productOption = createProductOption(product, inventory, 10000L);
        em.persist(productOption);

        User user = createUser();
        em.persist(user);

        em.flush();
        em.clear();

        OrderCreateRequest request = getOrderCreateRequest(productOption.getProductOptionId(), 2);

        //when & then
        assertThatThrownBy(()->orderService.createPendingOrder(request, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("현재 판매 불가능한 상품이 포함되어 있습니다");
    }

    @DisplayName("상품 옵션의 재고 수량보다 큰 수량을 차감할 경우 예외를 발생한다.")
    @Test
    void createOrderMoreThanInventory(){
        //given
        Brand brand = createBrand();
        em.persist(brand);

        Product product = createProduct(brand, true);
        em.persist(product);

        int stockQuantity = 0;

        Inventory inventory = createInventory(stockQuantity);
        em.persist(inventory);

        ProductOption productOption = createProductOption(product, inventory, 10000L);
        em.persist(productOption);

        User user = createUser();
        em.persist(user);

        em.flush();
        em.clear();

        OrderCreateRequest request = getOrderCreateRequest(productOption.getProductOptionId(), 1);

        //when & then
        Throwable thrown = catchThrowable(() -> orderService.createPendingOrder(request, user.getId()));

        //BusinessException 인스턴스인지 확인
        assertThat(thrown).isInstanceOf(BusinessException.class);
        BusinessException businessException = (BusinessException) thrown;

        //메시지 확인
        assertThat(businessException).hasMessage("재고가 부족한 상품이 있습니다");

        //예외 반환 데이터 확인
        Object data = businessException.getData();
        assertThat(data).isNotNull();
        List<InsufficientStockItem> insufficientItems = (List<InsufficientStockItem>) data;

        assertThat(insufficientItems).hasSize(1);
        assertThat(insufficientItems.get(0).getProductOptionId()).isEqualTo(productOption.getProductOptionId());
        assertThat(insufficientItems.get(0).getAvailableQuantity()).isEqualTo(stockQuantity);
    }

    private Inventory createInventory(int stockQuantity) {
        return Inventory.builder()
                .stockQuantity(new StockQuantity(stockQuantity))
                .build();
    }

    private Brand createBrand() {
        return Brand.builder()
                .nameKo("테스트 브랜드")
                .nameEn("Test Brand")
                .status(BrandStatus.ACTIVE)
                .commissionRate(new java.math.BigDecimal("10.00"))
                .logoUrl("https://example.com/logo.jpg")
                .build();
    }

    private Product createProduct(Brand brand, boolean isValid) {
        return Product.builder()
                .brand(brand)
                .productName("테스트 상품")
                .productInfo("테스트 상품 설명")
                .productGenderType(ProductGenderType.ALL)
                .brandName(brand.getNameKo())
                .categoryPath("상의/티셔츠")
                .isAvailable(isValid)
                .build();
    }

    private ProductOption createProductOption(Product product, Inventory inventory, Long price) {
        return ProductOption.builder()
                .product(product)
                .productPrice(new Money(price))
                .inventory(inventory)
                .build();
    }

    private User createUser() {
        return User.builder()
                .userName("testUser")
                .password("password123")
                .userEmail("test@example.com")
                .contactNumber("010-1234-5678")
                .role(UserRole.USER)
                .currentAddress("서울시 강남구")
                .avatarUrl("https://example.com/avatar.jpg")
                .isActive(true)
                .build();
    }

    private static OrderCreateRequest getOrderCreateRequest(Long productOptionId, int quantity) {
        OrderCreateItem item = OrderCreateItem.builder()
                .productOptionId(productOptionId)
                .quantity(quantity)
                .build();

        List<OrderCreateItem> items = List.of(item);

        return OrderCreateRequest.builder()
                .items(items)
                .build();
    }
}