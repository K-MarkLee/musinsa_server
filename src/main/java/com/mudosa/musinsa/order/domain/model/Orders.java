package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import com.mudosa.musinsa.order.application.dto.OrderCreateResponse;
import com.mudosa.musinsa.payment.domain.model.Payment;
import com.mudosa.musinsa.payment.domain.model.PaymentEventType;
import com.mudosa.musinsa.payment.domain.model.PaymentStatus;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/* Order가 Order 도메인의 Aggregate Root이다.
* 앞으로 OrderProduct랑 대화는 Order에서 한다. */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "coupon_id")
    private Long couponId;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus status;
    
    @Column(name = "order_no", nullable = false, length = 50, unique = true)
    private String orderNo;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "total_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    
    @Column(name = "final_payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPaymentAmount;
    
    @Column(name = "is_settleable", nullable = false)
    private Boolean isSettleable = false;
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public static Orders create(
            BigDecimal totalPrice,
            User user,
            Long couponId
            ) {

        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_AMOUNT);
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Orders order = new Orders();
        order.user = user;
        order.totalPrice = totalPrice;
        order.finalPaymentAmount = totalPrice;
        order.status = OrderStatus.PENDING;
        order.couponId = couponId;

        /* order 무작위 번호 생성 */
        order.orderNo = createOrderNumber();
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

    /* 주문 상품 검증 */
    public void validateOrderProducts() {
        if (this.orderProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 각 주문 상품의 상품 옵션 검증
        for (OrderProduct orderProduct : this.orderProducts) {
            orderProduct.validateProductOption();
        }
    }

    public void complete() {
        this.status = this.status.transitionTo(OrderStatus.COMPLETED);
        this.isSettleable = true;
    }

    public void rollback() {
        if (this.status.isCompleted()) {
            this.status = OrderStatus.PENDING;
            this.isSettleable = false;
        }
    }

    /* 재고 차감 */
    public void decreaseStock() {
        for (OrderProduct orderProduct : this.orderProducts) {
            orderProduct.decreaseStock();
        }
    }

    /* 쿠폰 사용 여부 확인 */
    public boolean hasCoupon() {
        return this.couponId != null;
    }

    /* 주문에 쿠폰 적용 */
    public void addCoupon(Long couponId){
        this.couponId = couponId;
    }

    /* 재고 복구 */
    public void restoreStock() {
        for (OrderProduct orderProduct : this.orderProducts) {
            orderProduct.restoreStock();
        }
    }

    /* 할인 적용 */
    public void applyDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_AMOUNT,"할인 금액은 음수일 수 없습니다");
        }

        if (discount.compareTo(this.totalPrice) > 0) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_AMOUNT,
                    "할인 금액이 총 금액보다 클 수 없습니다");
        }

        this.totalDiscount = discount;
        this.finalPaymentAmount = this.totalPrice.subtract(discount);
    }

    /* 주문 아이템 추가 */
    public void addOrderProduct(OrderProduct orderProduct) {
        if (orderProduct == null) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND, "주문 아이템이 null입니다");
        }
        this.orderProducts.add(orderProduct);
        orderProduct.setOrders(this);  // 양방향 연관관계 설정
    }

    /* 주문 아이템 일괄 추가 */
    public void addOrderProducts(List<OrderProduct> orderProducts) {
        if (orderProducts == null || orderProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND, "주문 아이템이 비어있습니다");
        }
        for (OrderProduct orderProduct : orderProducts) {
            addOrderProduct(orderProduct);
        }
    }
}
