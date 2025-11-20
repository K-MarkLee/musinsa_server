package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemRepositoryImplTest extends ServiceConfig {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long userId;
    private Long productOptionId1;

    @BeforeEach
    void setUp() {
        //given
        User user = userRepository.save(createUser());
        userId = user.getId();

        Brand brand = brandRepository.save(createBrand());
        Product product = productRepository.save(createProduct(brand, true));
        Inventory inventory = createInventory(1);
        ProductOption productOption1 = productOptionRepository.save(
                createProductOption(product, inventory, 10000L)
        );

        productOptionId1 = productOption1.getProductOptionId();
        CartItem cartItem1 = createCartItem(productOption1, user);
        cartItemRepository.save(cartItem1);
    }

    @DisplayName("사용자의 장바구니에서 지정한 상품 옵션 리스트를 삭제한다. ")
    @Test
    void deleteByUserIdAndProductOptionIdIn(){
        //when
        cartItemRepository.deleteByUserIdAndProductOptionIdIn(userId, List.of(productOptionId1));

        //then
        List<CartItem> cartItem = cartItemRepository.findAllByUserId(userId);
        assertThat(cartItem).isEmpty();
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

    private static CartItem createCartItem(ProductOption productOption, User user){
        return CartItem.builder()
                .user(user)
                .quantity(3)
                .productOption(productOption)
                .build();
    }
}