package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.product.application.dto.CartItemCreateRequest;
import com.mudosa.musinsa.product.application.dto.CartItemDetailResponse;
import com.mudosa.musinsa.product.application.dto.CartItemResponse;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.repository.CartItemRepository;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.common.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("CartService 단위 테스트")
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService sut;

    private void setId(Object target, String fieldName, Long id) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("장바구니 항목 추가")
    class AddCartItemTests {

        @Test
        @DisplayName("Given: 존재하지 않는 사용자면 BusinessException을 던진다")
        void add_userNotFound_shouldThrow() {
            // Given: userRepository가 비어있는 Optional 반환
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(10L).quantity(1).build()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 존재하지 않는 상품옵션이면 BusinessException을 던진다")
        void add_optionNotFound_shouldThrow() {
            // Given: 유효한 사용자
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            given(productOptionRepository.findByIdWithProductAndInventory(anyLong())).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(10L).quantity(1).build()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 유효한 사용자와 옵션이면 새 CartItem을 생성해 반환한다")
        void add_success_createNew() {
            // Given: user, product, option, inventory 준비
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            setId(product, "productId", 2L);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 33L);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(33L))).willReturn(Optional.of(po));

            given(cartItemRepository.findByUserIdAndProductOptionId(eq(1L), eq(33L))).willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItem.class))).willAnswer(i -> i.getArgument(0));

            // When: addCartItem 호출
            CartItemResponse resp = sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(33L).quantity(2).build());

            // Then: 응답에 수량과 옵션 id가 반영된다
            assertThat(resp.getProductOptionId()).isEqualTo(33L);
            assertThat(resp.getQuantity()).isEqualTo(2);
            then(cartItemRepository).should().save(any(CartItem.class));
        }

        @Test
        @DisplayName("Given: 이미 장바구니에 있으면 수량을 합산해 저장한다")
        void add_existing_updatesQuantity() {
            // Given
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 44L);

            CartItem existing = CartItem.builder().user(user).productOption(po).quantity(1).build();
            setId(existing, "cartItemId", 5L);

            given(productOptionRepository.findByIdWithProductAndInventory(eq(44L))).willReturn(Optional.of(po));
            given(cartItemRepository.findByUserIdAndProductOptionId(eq(1L), eq(44L))).willReturn(Optional.of(existing));
            given(cartItemRepository.save(any(CartItem.class))).willAnswer(i -> i.getArgument(0));

            // When
            CartItemResponse resp = sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(44L).quantity(3).build());

            // Then: 기존 수량 1 + 3 = 4
            assertThat(resp.getQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("Given: 요청 수량이 재고를 초과하면 예외를 던진다")
        void add_new_exceedsStock_shouldThrow() {
            // Given: user와 productOption(재고 2) 준비
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 88L);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(88L))).willReturn(Optional.of(po));

            // When / Then: 요청 수량 5로 인해 재고 부족 예외 발생
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(88L).quantity(5).build()))
                    .isInstanceOf(BusinessException.class);
        }

    @Test
    @DisplayName("Given: 요청 수량이 매우 큰 값이면 재고 부족 예외가 발생한다")
    void add_new_largeQuantity_shouldThrow() {
        // Given: user와 productOption(재고 10) 준비
        User user = User.create("u","p","a@b.com", null, null, null, null);
        setId(user, "id", 1L);
        given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

        Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
        ProductOption po = ProductOption.create(product, new Money(1000), inv);
        setId(po, "productOptionId", 555L);
        given(productOptionRepository.findByIdWithProductAndInventory(eq(555L))).willReturn(Optional.of(po));

        // When / Then: 매우 큰 수량 요청 시 재고 부족 예외 발생
        assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(555L).quantity(Integer.MAX_VALUE).build()))
                .isInstanceOf(BusinessException.class);
    }

        @Test
        @DisplayName("Given: 상품이 판매중이지 않으면 예외를 던진다")
        void add_productNotAvailable_shouldThrow() {
            // Given: user와 판매중이지 않은 product
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(false).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 99L);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(99L))).willReturn(Optional.of(po));

            // When / Then
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(99L).quantity(1).build()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 요청 수량이 0 이하이면 검증 예외를 던진다")
        void add_invalidQuantity_shouldThrow() {
            // Given: 유효한 user와 option
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 100L);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(100L))).willReturn(Optional.of(po));

            // When / Then: 0개 요청 시 예외 발생
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(100L).quantity(0).build()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 기존 장바구니 수량과 추가 수량의 합이 재고를 초과하면 예외를 던진다")
        void add_existing_combinedExceedsStock_shouldThrow() {
            // Given: user, existing cartItem quantity 3, stock 4, 추가 요청 2 => 합 5 > stock
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(4)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 111L);

            CartItem existing = CartItem.builder().user(user).productOption(po).quantity(3).build();
            setId(existing, "cartItemId", 123L);

            given(productOptionRepository.findByIdWithProductAndInventory(eq(111L))).willReturn(Optional.of(po));
            given(cartItemRepository.findByUserIdAndProductOptionId(eq(1L), eq(111L))).willReturn(Optional.of(existing));

            // When / Then: 추가 수량 2로 인해 합산 후 재고 초과 예외
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(111L).quantity(2).build()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: productOption의 재고 정보가 없으면 예외를 던진다")
        void add_inventoryNull_shouldThrow() {
            // Given: inventory가 null인 productOption
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            ProductOption po = org.mockito.Mockito.mock(ProductOption.class);
            given(po.getProduct()).willReturn(product);
            given(po.getInventory()).willReturn(null);
            given(po.getProductOptionId()).willReturn(199L);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(199L))).willReturn(Optional.of(po));

            // When / Then
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(199L).quantity(1).build()))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 저장 중 예외가 발생하면 예외를 전파한다")
        void add_saveThrows_shouldPropagate() {
            // Given: user와 option 준비
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);
            given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 333L);
            given(productOptionRepository.findByIdWithProductAndInventory(eq(333L))).willReturn(Optional.of(po));

            given(cartItemRepository.findByUserIdAndProductOptionId(eq(1L), eq(333L))).willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItem.class))).willThrow(new RuntimeException("DB error"));

            // When / Then
            assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(333L).quantity(1).build()))
                    .isInstanceOf(RuntimeException.class);
        }

    @Test
    @DisplayName("Given: productOption의 product가 null이면 예외를 던진다")
    void add_productNull_shouldThrow() {
        // Given: inventory는 존재하지만 product가 null인 ProductOption
        User user = User.create("u","p","a@b.com", null, null, null, null);
        setId(user, "id", 1L);
        given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

    ProductOption po = org.mockito.Mockito.mock(ProductOption.class);
    given(po.getProduct()).willReturn(null);
        given(productOptionRepository.findByIdWithProductAndInventory(eq(333L))).willReturn(Optional.of(po));

        // When / Then
        assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(333L).quantity(1).build()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Given: 요청 수량이 음수이면 검증 예외를 던진다")
    void add_negativeQuantity_shouldThrow() {
        // Given: 유효한 user와 option
        User user = User.create("u","p","a@b.com", null, null, null, null);
        setId(user, "id", 1L);
        given(userRepository.findById(eq(1L))).willReturn(Optional.of(user));

        Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
        ProductOption po = ProductOption.create(product, new Money(1000), inv);
        setId(po, "productOptionId", 444L);
        given(productOptionRepository.findByIdWithProductAndInventory(eq(444L))).willReturn(Optional.of(po));

        // When / Then: 음수 수량 요청 시 예외 발생
        assertThatThrownBy(() -> sut.addCartItem(1L, CartItemCreateRequest.builder().productOptionId(444L).quantity(-3).build()))
                .isInstanceOf(BusinessException.class);
    }
    }

    @Nested
    @DisplayName("장바구니 조회")
    class GetCartTests {

        @Test
        @DisplayName("Given: 사용자의 장바구니 항목이 있으면 상세 목록을 반환한다")
        void getCartItems_success() {
            // Given: 하나의 CartItem이 반환되도록 설정
            User user = User.create("u","p","a@b.com", null, null, null, null);
            setId(user, "id", 1L);

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(3)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 77L);

            CartItem item = CartItem.builder().user(user).productOption(po).quantity(2).build();
            setId(item, "cartItemId", 9L);

            given(cartItemRepository.findAllWithDetailsByUserId(eq(1L))).willReturn(List.of(item));

            // When
            List<CartItemDetailResponse> list = sut.getCartItems(1L);

            // Then
            assertThat(list).hasSize(1);
            assertThat(list.get(0).getProductOptionId()).isEqualTo(77L);
        }

        @Test
        @DisplayName("Given: 사용자의 장바구니가 비어있으면 빈 리스트를 반환한다")
        void getCartItems_empty_returnsEmpty() {
            // Given
            given(cartItemRepository.findAllWithDetailsByUserId(eq(2L))).willReturn(List.of());

            // When
            List<CartItemDetailResponse> list = sut.getCartItems(2L);

            // Then
            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("수량 수정")
    class UpdateQuantityTests {

        @Test
        @DisplayName("Given: 0 이하 수량을 전달하면 BusinessException을 던진다")
        void updateQuantity_invalid_shouldThrow() {
            // When / Then
            assertThatThrownBy(() -> sut.updateCartItemQuantity(1L, 10L, 0))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 장바구니 항목이 없으면 BusinessException을 던진다")
        void updateQuantity_notFound_shouldThrow() {
            // Given
            given(cartItemRepository.findById(anyLong())).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> sut.updateCartItemQuantity(1L, 999L, 2))
                    .isInstanceOf(BusinessException.class);
        }

    @Test
    @DisplayName("Given: productOption 조회 시 옵션이 없으면 예외를 던진다")
    void update_productOptionNotFound_shouldThrow() {
        // Given: 존재하는 cartItem이 있으나 productOption 조회가 실패
        User owner = User.create("o","p","o@b.com", null, null, null, null);
        setId(owner, "id", 2L);

        Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
        Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
        ProductOption po = ProductOption.create(product, new Money(1000), inv);
        setId(po, "productOptionId", 600L);

        CartItem item = CartItem.builder().user(owner).productOption(po).quantity(1).build();
        setId(item, "cartItemId", 77L);

        given(cartItemRepository.findById(eq(77L))).willReturn(Optional.of(item));
        given(productOptionRepository.findByIdWithProductAndInventory(eq(600L))).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> sut.updateCartItemQuantity(2L, 77L, 2))
            .isInstanceOf(BusinessException.class);
    }

        @Test
        @DisplayName("Given: 본인 소유가 아니면 Forbidden 예외를 던진다")
        void updateQuantity_forbidden_shouldThrow() {
            // Given: cartItem 소유자 id와 호출 userId 불일치
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 2L);

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);

            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(1).build();
            setId(item, "cartItemId", 20L);

            given(cartItemRepository.findById(eq(20L))).willReturn(Optional.of(item));

            // When / Then: 호출자는 userId=1이지만 실제 소유자 id는 2
            assertThatThrownBy(() -> sut.updateCartItemQuantity(1L, 20L, 2))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 재고 부족이면 BusinessException을 던진다")
        void updateQuantity_insufficientStock_shouldThrow() {
            // Given: owner와 cartItem, productOption 재고 적음
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 2L);

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(1)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 55L);

            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(1).build();
            setId(item, "cartItemId", 21L);

            given(cartItemRepository.findById(eq(21L))).willReturn(Optional.of(item));
            given(productOptionRepository.findByIdWithProductAndInventory(eq(55L))).willReturn(Optional.of(po));

            // When / Then: 요청 수량 5로 인한 재고 부족
            assertThatThrownBy(() -> sut.updateCartItemQuantity(2L, 21L, 5))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 정상적인 요청이면 수량이 변경되어 저장되고 응답이 반환된다")
        void updateQuantity_success() {
            // Given
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 2L);

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 60L);

            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(2).build();
            setId(item, "cartItemId", 22L);

            given(cartItemRepository.findById(eq(22L))).willReturn(Optional.of(item));
            given(productOptionRepository.findByIdWithProductAndInventory(eq(60L))).willReturn(Optional.of(po));
            given(cartItemRepository.save(any(CartItem.class))).willAnswer(i -> i.getArgument(0));

            // When
            CartItemResponse resp = sut.updateCartItemQuantity(2L, 22L, 5);

            // Then
            assertThat(resp.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Given: 같은 수량으로 요청하면 저장이 호출되고 결과 수량은 동일하다 (현재 동작)")
        void updateQuantity_sameQuantity_callsSaveAndNoChange() {
            // Given
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 2L);

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(10)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            setId(po, "productOptionId", 60L);

            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(5).build();
            setId(item, "cartItemId", 22L);

            given(cartItemRepository.findById(eq(22L))).willReturn(Optional.of(item));
            given(productOptionRepository.findByIdWithProductAndInventory(eq(60L))).willReturn(Optional.of(po));
            given(cartItemRepository.save(any(CartItem.class))).willAnswer(i -> i.getArgument(0));

            // When
            CartItemResponse resp = sut.updateCartItemQuantity(2L, 22L, 5);

            // Then
            then(cartItemRepository).should().save(any(CartItem.class));
            assertThat(resp.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Given: 업데이트 시 productOption의 재고 정보가 없으면 예외를 던진다")
        void update_inventoryNull_shouldThrow() {
            // Given: cartItem에 연결된 productOption의 inventory가 null
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 2L);

            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            ProductOption po = org.mockito.Mockito.mock(ProductOption.class);
            given(po.getProduct()).willReturn(product);
            given(po.getInventory()).willReturn(null);
            given(po.getProductOptionId()).willReturn(222L);

            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(2).build();
            setId(item, "cartItemId", 222L);

            given(cartItemRepository.findById(eq(222L))).willReturn(Optional.of(item));
            given(productOptionRepository.findByIdWithProductAndInventory(anyLong())).willReturn(Optional.of(po));

            // When / Then: 재고 정보가 없으므로 예외
            assertThatThrownBy(() -> sut.updateCartItemQuantity(2L, 222L, 3))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("장바구니 삭제")
    class DeleteTests {

        @Test
        @DisplayName("Given: 삭제할 옵션 리스트가 비어있으면 아무 작업도 하지 않는다")
        void deleteByProductOptions_empty_noop() {
            // Given / When
            sut.deleteCartItemsByProductOptions(1L, List.of());

            // Then: repository가 호출되지 않아야 함
            org.mockito.Mockito.verifyNoInteractions(cartItemRepository);
        }


        @Test
        @DisplayName("Given: 존재하지 않는 cartItem을 삭제하면 예외가 발생한다")
        void deleteCartItem_notFound_shouldThrow() {
            // Given
            given(cartItemRepository.findById(anyLong())).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> sut.deleteCartItem(1L, 999L)).isInstanceOf(BusinessException.class);
        }


        @Test
        @DisplayName("Given: 삭제할 옵션 리스트가 null이면 아무 작업도 하지 않는다")
        void deleteByProductOptions_null_noop() {
            // When
            sut.deleteCartItemsByProductOptions(1L, null);

            // Then: repository가 호출되지 않아야 함
            org.mockito.Mockito.verifyNoInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("Given: 본인 소유가 아니면 삭제 불가")
        void deleteCartItem_forbidden_shouldThrow() {
            // Given: cartItem 소유자 다름
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 2L);
            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(1).build();
            setId(item, "cartItemId", 30L);

            given(cartItemRepository.findById(eq(30L))).willReturn(Optional.of(item));

            // When / Then
            assertThatThrownBy(() -> sut.deleteCartItem(1L, 30L)).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: cartItem의 소유자 정보가 없으면 삭제 불가 예외를 던진다")
        void deleteCartItem_userNull_shouldThrow() {
            // Given: cartItem에 user가 null - construct a mock CartItem to simulate repository returning an invalid state
            CartItem item = org.mockito.Mockito.mock(CartItem.class);
            given(item.getUser()).willReturn(null);

            given(cartItemRepository.findById(eq(40L))).willReturn(Optional.of(item));

            // When / Then
            assertThatThrownBy(() -> sut.deleteCartItem(1L, 40L)).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Given: 본인 소유이면 삭제가 수행된다")
        void deleteCartItem_success() {
            // Given
            User owner = User.create("o","p","o@b.com", null, null, null, null);
            setId(owner, "id", 1L);
            Product product = Product.builder().brand(null).productName("p").productInfo("i").productGenderType(ProductGenderType.ALL).brandName("b").categoryPath("c").isAvailable(true).build();
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption po = ProductOption.create(product, new Money(1000), inv);
            CartItem item = CartItem.builder().user(owner).productOption(po).quantity(1).build();
            setId(item, "cartItemId", 31L);

            given(cartItemRepository.findById(eq(31L))).willReturn(Optional.of(item));

            // When
            sut.deleteCartItem(1L, 31L);

            // Then: delete 메서드 호출
            then(cartItemRepository).should().delete(item);
        }
    }
}
