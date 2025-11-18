package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import com.mudosa.musinsa.order.application.dto.OrderCreateItem;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Order extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    private Long userId;
    
    private Long couponId;

    //TODO: order가 order_product를 바라보는 상황이 있을까?
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name="order_status")
    private OrderStatus status;
    
    private String orderNo;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_price"))
    private Money totalPrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_discount"))
    private Money totalDiscount = Money.of(BigDecimal.ZERO);

    @CreatedDate
    private LocalDateTime registeredAt;

    private Boolean isSettleable = false;
    
    private LocalDateTime settledAt;

    @Builder
    private Order(Long userId, Long couponId, OrderStatus status, String orderNo, Money totalPrice, Money totalDiscount, LocalDateTime registeredAt, Boolean isSettleable, LocalDateTime settledAt, List<OrderProduct> orderProducts) {
        this.userId = userId;
        this.couponId = couponId;
        this.status = status;
        this.orderNo = orderNo;
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
        this.registeredAt = registeredAt;
        this.isSettleable = isSettleable;
        this.settledAt = settledAt;
        this.orderProducts = orderProducts;
    }

    public static Order create(
            Long userId,
            Long couponId,
            Map<ProductOption, Integer> orderProductsWithQuantity
    ) {
        if(orderProductsWithQuantity == null){
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND, "상품 목록이 없는 주문은 생성할 수 없습니다");
        }

        //Order 생성
        Order order = Order.builder()
                .userId(userId)
                .orderNo(createOrderNumber())
                .status(OrderStatus.PENDING)
                .couponId(couponId)
                .orderProducts(new ArrayList<>())
                .build();

        //총 가격 초기 세팅
        Money calculatedTotalPrice = Money.of(0L);

        for(Map.Entry<ProductOption, Integer> entry: orderProductsWithQuantity.entrySet()){
            ProductOption productOption = entry.getKey();
            Integer quantity = entry.getValue();
            if(quantity < 1) throw new BusinessException(ErrorCode.ORDER_CREATE_FAIL, "상품은 1개 이상 주문 가능합니다");

            //OrderProduct 생성
            OrderProduct orderProduct = OrderProduct.create(order, productOption, quantity);
            order.orderProducts.add(orderProduct);

            //총 가격 계산
            Money productPrice = productOption.getProductPrice();
            Money itemTotalPrice = productPrice.multiply(quantity);
            calculatedTotalPrice = calculatedTotalPrice.add(itemTotalPrice);
        }

        order.totalPrice = calculatedTotalPrice;
        return order;
    }


    private static String createOrderNumber(){
        // 주문번호 형식: ORD + timestamp(13자리) + random(3자리)
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 1000);
        return String.format("ORD%d%03d", timestamp, random);
    }

    public void validatePending() {
        if (!this.status.isPending()) {
            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_STATUS,
                    String.format("주문 상태가 PENDING이 아닙니다. 현재: %s", this.status)
            );
        }
    }

    public void complete() {
        validatePending();
        this.status = this.status.transitionTo(OrderStatus.COMPLETED);
        this.isSettleable = true;
    }

    public void rollback() {
        if (this.status.isCompleted()) {
            this.status = OrderStatus.PENDING;
            this.isSettleable = false;
        }
    }

    public void restoreStock() {
        for (OrderProduct orderProduct : this.orderProducts) {
            orderProduct.restoreStock();
        }
    }

}
