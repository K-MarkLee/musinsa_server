package com.mudosa.musinsa.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.CartItemCreateRequest;
import com.mudosa.musinsa.product.application.dto.CartItemDetailResponse;
import com.mudosa.musinsa.product.application.dto.CartItemResponse;
import com.mudosa.musinsa.product.domain.model.CartItem;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.CartItemRepository;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("사용자 장바구니를 조회하면 상품 상세 정보와 함께 반환한다")
    void getCartItems_returnsDetailedResponses() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(true);
        ProductOption option1 = createProductOption(product, 5);
        ProductOption option2 = createProductOption(product, 7);
        ReflectionTestUtils.setField(option1, "productOptionId", 2101L);
        ReflectionTestUtils.setField(option2, "productOptionId", 2102L);

        CartItem first = CartItem.builder()
            .user(user)
            .productOption(option1)
            .quantity(1)
            .unitPrice(option1.getProductPrice())
            .build();
        CartItem second = CartItem.builder()
            .user(user)
            .productOption(option2)
            .quantity(2)
            .unitPrice(option2.getProductPrice())
            .build();
        ReflectionTestUtils.setField(first, "cartItemId", 500L);
        ReflectionTestUtils.setField(second, "cartItemId", 501L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllWithDetailsByUserId(10L)).thenReturn(List.of(first, second));

        List<CartItemDetailResponse> responses = cartService.getCartItems(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("cartItemId").containsExactlyInAnyOrder(500L, 501L);
        assertThat(responses).extracting("productName").containsOnly("상품");
    }

    @Test
    @DisplayName("새 옵션을 장바구니에 담으면 신규 항목이 생성된다")
    void addCartItem_createsNewItem() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(true);
        ProductOption option = createProductOption(product, 5);
        ReflectionTestUtils.setField(option, "productOptionId", 2101L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productOptionRepository.findByIdWithProductAndInventory(2101L)).thenReturn(Optional.of(option));
        when(cartItemRepository.findByUserIdAndProductOptionId(10L, 2101L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "cartItemId", 900L);
            return saved;
        });

        CartItemResponse response = cartService.addCartItem(10L,
            CartItemCreateRequest.builder()
                .productOptionId(2101L)
                .quantity(2)
                .build());

        assertThat(response.getCartItemId()).isEqualTo(900L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getProductOptionId()).isEqualTo(2101L);
        assertThat(response.getQuantity()).isEqualTo(2);
    assertThat(response.getUnitPrice()).isEqualTo(new Money(BigDecimal.valueOf(15900)).getAmount());
    }

    @Test
    @DisplayName("동일 옵션을 다시 담으면 수량이 누적된다")
    void addCartItem_existingItemIncrementsQuantity() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(true);
        ProductOption option = createProductOption(product, 6);
        ReflectionTestUtils.setField(option, "productOptionId", 2101L);

        CartItem existing = CartItem.builder()
            .user(user)
            .productOption(option)
            .quantity(1)
            .unitPrice(option.getProductPrice())
            .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productOptionRepository.findByIdWithProductAndInventory(2101L)).thenReturn(Optional.of(option));
        when(cartItemRepository.findByUserIdAndProductOptionId(10L, 2101L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).thenReturn(existing);

        CartItemResponse response = cartService.addCartItem(10L,
            CartItemCreateRequest.builder()
                .productOptionId(2101L)
                .quantity(2)
                .build());

        assertThat(existing.getQuantity()).isEqualTo(3);
        assertThat(response.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니 수량을 수정하면 재고 검증 후 반영된다")
    void updateCartItemQuantity_updatesSuccessfully() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(true);
        ProductOption option = createProductOption(product, 10);
        ReflectionTestUtils.setField(option, "productOptionId", 2101L);

        CartItem cartItem = CartItem.builder()
            .user(user)
            .productOption(option)
            .quantity(2)
            .unitPrice(option.getProductPrice())
            .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", 201L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
    when(cartItemRepository.findById(201L)).thenReturn(Optional.of(cartItem));
        when(productOptionRepository.findByIdWithProductAndInventory(2101L)).thenReturn(Optional.of(option));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItemResponse response = cartService.updateCartItemQuantity(10L, 201L, 5);

        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 항목을 삭제하면 사용자 소유 항목만 제거된다")
    void deleteCartItem_removesEntry() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(true);
        ProductOption option = createProductOption(product, 4);
        ReflectionTestUtils.setField(option, "productOptionId", 2101L);

        CartItem cartItem = CartItem.builder()
            .user(user)
            .productOption(option)
            .quantity(1)
            .unitPrice(option.getProductPrice())
            .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", 300L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
    when(cartItemRepository.findById(300L)).thenReturn(Optional.of(cartItem));

        cartService.deleteCartItem(10L, 300L);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("재고를 초과해 장바구니에 담으면 예외가 발생한다")
    void addCartItem_exceedsStock_throwsException() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(true);
        ProductOption option = createProductOption(product, 2);
        ReflectionTestUtils.setField(option, "productOptionId", 2101L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productOptionRepository.findByIdWithProductAndInventory(2101L)).thenReturn(Optional.of(option));
        when(cartItemRepository.findByUserIdAndProductOptionId(10L, 2101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addCartItem(10L,
            CartItemCreateRequest.builder()
                .productOptionId(2101L)
                .quantity(3)
                .build()))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("판매 중지된 상품 옵션은 장바구니에 담을 수 없다")
    void addCartItem_inactiveProduct_throwsException() {
        User user = User.create("tester", "pw", "user@test.com", UserRole.USER, null);
        ReflectionTestUtils.setField(user, "id", 10L);

        Product product = createProduct(false);
        ProductOption option = createProductOption(product, 3);
        ReflectionTestUtils.setField(option, "productOptionId", 2101L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productOptionRepository.findByIdWithProductAndInventory(2101L)).thenReturn(Optional.of(option));

        assertThatThrownBy(() -> cartService.addCartItem(10L,
            CartItemCreateRequest.builder()
                .productOptionId(2101L)
                .quantity(1)
                .build()))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE);
    }

    private Product createProduct(boolean available) {
        Brand brand = Brand.builder()
            .brandId(1L)
            .nameKo("브랜드")
            .nameEn("BRAND")
            .status(BrandStatus.ACTIVE)
            .commissionRate(BigDecimal.TEN)
            .build();

        Product product = Product.builder()
            .brand(brand)
            .productName("상품")
            .productInfo("상품 설명")
            .productGenderType(ProductGenderType.MEN)
            .brandName("브랜드")
            .categoryPath("상의/티셔츠")
            .isAvailable(available)
            .build();
        return product;
    }

    private ProductOption createProductOption(Product product, int stock) {
        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(stock))
            .build();

        return ProductOption.builder()
            .product(product)
            .productPrice(new Money(BigDecimal.valueOf(15900)))
            .inventory(inventory)
            .build();
    }
}
