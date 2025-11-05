package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventOption;

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

    //재고 차감을 위해 필요함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;
    
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;
    
    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_option_id")
    private EventOption eventOption;

    @Column(name = "paid_flag", nullable = false)
    private Boolean paidFlag = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_scope")
    private LimitScope limitScope;

    // getProductOptionId 메서드 추가
    public Long getProductOptionId() {
        return productOption.getProductOptionId();  // productOption 객체의 ID 반환
    }

    // 양방향 연관관계 설정용 메서드
    void setOrders(Orders orders) {
        this.orders = orders;
    }

    public static OrderProduct create(
            Long userId,
            ProductOption productOption,
            BigDecimal productPrice,
            Integer productQuantity,
            Event event,
            EventOption eventOption,
            LimitScope limitScope) {

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.userId = userId;
        orderProduct.productOption = productOption;
        orderProduct.productPrice = productPrice;
        orderProduct.productQuantity = productQuantity;
        orderProduct.event = event;  // Event 객체
        orderProduct.eventOption = eventOption;  // EventOption 객체
        orderProduct.limitScope = limitScope;  // LimitScope enum
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

    /* 재고 확인 */
    public boolean hasEnoughStock() {
        return this.productOption.getInventory().isSufficientStock(this.productQuantity);
    }

    /* 재고 개수 확인 */
    public int getAvailableStock() {
        return this.productOption.getInventory().getStockQuantity().getValue();
    }
}
