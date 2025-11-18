package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventOption;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price"))
    private Money productPrice;

    private Integer productQuantity;

    @Builder
    private OrderProduct(Order order, ProductOption productOption, Money productPrice, Integer productQuantity) {
        this.order = order;
        this.productOption = productOption;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
    }

    public static OrderProduct create(Order order, ProductOption productOption, int productQuantity){
        return OrderProduct.builder()
                .order(order)
                .productPrice(productOption.getProductPrice())
                .productOption(productOption)
                .productQuantity(productQuantity)
                .build();
    }

    public Long getProductOptionId() {
        return productOption.getProductOptionId();  // productOption 객체의 ID 반환
    }

    public void setOrder(Order order) {
        this.order = order;
    }


    /* 재고 복구 */
    public void restoreStock() {
        this.productOption.restoreStock(this.productQuantity);
    }

    /* 재고 확인 */
    public boolean hasEnoughStock() {
        return this.productOption.getInventory().isSufficientStock(this.productQuantity);
    }

    /* 재고 개수 확인 */
    public int getAvailableStock() {
        return this.productOption.getInventory().getStockQuantity().getValue();
    }

    public Money calculatePrice() {
        return this.productPrice.multiply(BigDecimal.valueOf(this.productQuantity));
    }
}
