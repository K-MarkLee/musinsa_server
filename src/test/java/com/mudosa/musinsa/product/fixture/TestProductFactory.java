package com.mudosa.musinsa.product.fixture;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.user.domain.model.User;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public final class TestProductFactory {

    private TestProductFactory() {}

    public static Product createBasicProduct() {
        Brand brand = Brand.create("테스트브랜드", "test-brand", BigDecimal.ZERO);
        return Product.builder()
                .brand(brand)
                .productName("기본상품")
                .productInfo("기본 상품 정보")
                .productGenderType(ProductGenderType.ALL)
                .brandName(brand.getNameKo())
                .categoryPath("상의/티셔츠")
                .isAvailable(true)
                .build();
    }

    public static Inventory createInventory(int stock) {
        return Inventory.builder().stockQuantity(new StockQuantity(stock)).build();
    }

    public static ProductOption createProductOption(Product product, long optionId, int stock) {
        Inventory inv = createInventory(stock);
        ProductOption po = ProductOption.create(product, new Money(1000), inv);
        // set id via reflection for tests
        try {
            Field f = po.getClass().getDeclaredField("productOptionId");
            f.setAccessible(true);
            f.set(po, optionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return po;
    }

    public static User createUser(String username, long id) {
        User u = User.create(username, "pw", username + "@example.com", null, null, null, null);
        try {
            Field f = u.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(u, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }
}
