package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
    private Orders orders;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;

    //재고 차감을 위해 필요함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;
    
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;
    
    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity = 1;
    
    @Column(name = "event_id")
    private Long eventId;
    
    @Column(name = "event_option_id")
    private Long eventOptionId;
    
    @Column(name = "paid_flag", nullable = false)
    private Boolean paidFlag = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_scope")
    private LimitScope limitScope;

    public static OrderProduct create(
            Long userId,
            Long productId,
            ProductOption productOption,
            BigDecimal productPrice,
            Integer productQuantity,
            Long eventId,
            Long eventOptionId,
            LimitScope limitScope) {

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.userId = userId;
        orderProduct.productId = productId;
        orderProduct.productOption = productOption;
        orderProduct.productPrice = productPrice;
        orderProduct.productQuantity = productQuantity;
        orderProduct.eventId = eventId;
        orderProduct.eventOptionId = eventOptionId;
        orderProduct.limitScope = limitScope;
        orderProduct.paidFlag = false;
        return orderProduct;
    }

    /* 재고 차감 */
    public void decreaseStock() {
        this.productOption.decreaseStock(this.productQuantity);
    }

    /* 재고 복구 */
    public void restoreStock() {
        this.productOption.restoreStock(this.productQuantity);
    }

    /* 상품 옵션 검증 */
    public void validateProductOption() {
        this.productOption.validateAvailable();
    }
}
