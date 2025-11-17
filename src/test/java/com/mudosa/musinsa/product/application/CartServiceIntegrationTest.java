package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.CartItemCreateRequest;
import com.mudosa.musinsa.product.application.dto.CartItemDetailResponse;
import com.mudosa.musinsa.product.application.dto.CartItemResponse;
import com.mudosa.musinsa.product.domain.model.CartItem;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.CartItemRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CartService를 실제 DB/트랜잭션에서 검증하여
 * 상품/옵션/재고와의 연동이 정상 동작하는지 확인한다.
 */
@DisplayName("CartService 통합 테스트")
class CartServiceIntegrationTest extends ServiceConfig {

    @Autowired
    private CartService cartService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BrandRepository brandRepository;

    private User createUser(String username) {
        return userRepository.save(User.create(
            username,
            "Password123!",
            username + "@test.com",
            UserRole.USER,
            null,
            "010-1234-5678",
            "서울시 성동구"));
    }

    private ProductOption createProductOption(int stockQuantity) {
        Brand brand = brandRepository.save(Brand.create("카트브랜드", "cart-brand", BigDecimal.ONE));
        Product product = productRepository.save(Product.builder()
            .brand(brand)
            .productName("장바구니 상품")
            .productInfo("통합 테스트용 상품")
            .productGenderType(ProductGenderType.ALL)
            .brandName(brand.getNameKo())
            .categoryPath("상의>후드티")
            .isAvailable(true)
            .build());

        Inventory inventory = Inventory.builder()
            .stockQuantity(new StockQuantity(stockQuantity))
            .build();
        ProductOption option = ProductOption.create(product, new Money(15000L), inventory);
        product.addProductOption(option);
        return productRepository.save(product).getProductOptions().get(0);
    }

    @Test
    @DisplayName("장바구니에 상품 옵션을 추가하면 CartItem이 저장되고 응답을 반환한다")
    void addCartItem_persistsEntity() {
        User user = createUser("cart-user1");
        ProductOption option = createProductOption(5);

        CartItemResponse response = cartService.addCartItem(user.getId(),
            CartItemCreateRequest.builder()
                .productOptionId(option.getProductOptionId())
                .quantity(2)
                .build());

        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(cartItemRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("장바구니 항목을 조회하면 상품 정보와 썸네일까지 포함된 응답을 반환한다")
    void getCartItems_returnsDetailedResponse() {
        User user = createUser("cart-user2");
        ProductOption option = createProductOption(3);

        cartService.addCartItem(user.getId(), CartItemCreateRequest.builder()
            .productOptionId(option.getProductOptionId())
            .quantity(1)
            .build());

        List<CartItemDetailResponse> details = cartService.getCartItems(user.getId());

        assertThat(details).hasSize(1);
        CartItemDetailResponse detail = details.get(0);
        assertThat(detail.getProductOptionId()).isEqualTo(option.getProductOptionId());
        assertThat(detail.getHasStock()).isTrue();
    }

    @Test
    @DisplayName("장바구니 수량을 수정하면 DB 상태가 갱신된다")
    void updateCartItemQuantity_updatesEntity() {
        User user = createUser("cart-user3");
        ProductOption option = createProductOption(10);

        CartItem cartItem = cartItemRepository.save(CartItem.builder()
            .user(user)
            .productOption(option)
            .quantity(1)
            .build());

        CartItemResponse response = cartService.updateCartItemQuantity(
            user.getId(),
            cartItem.getCartItemId(),
            4);

        assertThat(response.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("여러 옵션 ID를 전달해 장바구니 항목을 일괄 삭제할 수 있다")
    void deleteCartItemsByProductOptions_removesRows() {
        User user = createUser("cart-user4");
        ProductOption option1 = createProductOption(2);
        ProductOption option2 = createProductOption(2);

        CartItem item1 = cartItemRepository.save(CartItem.builder()
            .user(user)
            .productOption(option1)
            .quantity(1)
            .build());
        CartItem item2 = cartItemRepository.save(CartItem.builder()
            .user(user)
            .productOption(option2)
            .quantity(1)
            .build());

        cartService.deleteCartItemsByProductOptions(user.getId(),
            List.of(option1.getProductOptionId(), option2.getProductOptionId()));

        assertThat(cartItemRepository.findById(item1.getCartItemId())).isEmpty();
        assertThat(cartItemRepository.findById(item2.getCartItemId())).isEmpty();
    }
}
