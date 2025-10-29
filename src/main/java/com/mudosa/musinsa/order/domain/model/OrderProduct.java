package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventOption;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.user.domain.model.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



@Getter
@Entity
@Table(
        name = "order_product",
        indexes = {
                @Index(name = "idx_op_order", columnList = "order_id"),
                @Index(name = "idx_op_user", columnList = "user_id"),
                @Index(name = "idx_op_product", columnList = "product_id"),
                @Index(name = "idx_op_option", columnList = "product_option_id"),
                @Index(name = "idx_op_event", columnList = "event_id"),
                @Index(name = "idx_op_paid", columnList = "paid_flag"),
                @Index(name = "idx_op_limit_scope", columnList = "limit_scope")  // limit_scope에 대한 인덱스 추가
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_paid_once_per_event", columnNames = {"event_id", "user_id", "paid_flag", "limit_scope"}),
                @UniqueConstraint(name = "uq_paid_once_per_event_option", columnNames = {"event_option_id", "user_id", "paid_flag", "limit_scope"})
        }
)

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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

    // OrderProduct 객체를 생성하는 정적 팩토리 메서드
    public static OrderProduct create(
            Order order,
            User user,
            Product product,
            ProductOption productOption,
            BigDecimal productPrice,
            Integer productQuantity,
            Event event,
            EventOption eventOption,
            LimitScope limitScope
    ) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.order = order;  // Order 객체를 `order` 필드에 대입
        orderProduct.user = user;  // User 객체
        orderProduct.product = product;  // Product 객체
        orderProduct.productOption = productOption;  // ProductOption 객체
        orderProduct.productPrice = productPrice;
        orderProduct.productQuantity = productQuantity;
        orderProduct.event = event;  // Event 객체
        orderProduct.eventOption = eventOption;  // EventOption 객체
        orderProduct.limitScope = limitScope;  // LimitScope enum
        orderProduct.paidFlag = false;
        return orderProduct;
    }

}
