package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
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
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Orders extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    //TODO: 굳이 사용자를 바라봐야하는가?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "coupon_id")
    private Long couponId;

    //TODO: order가 order_product를 바라보는 상황이 있을까?
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus status;
    
    @Column(name = "order_no", nullable = false, length = 50, unique = true)
    private String orderNo;

    //TODO: 값 객체를 도입해야 한다.
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "total_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "is_settleable", nullable = false)
    private Boolean isSettleable = false;
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    /* 정적 팩토리 메서드를 사용해서 객체 생성 */
    public static Orders create(
            User user,
            Long couponId
            ) {

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Orders order = new Orders();
        order.user = user;
        order.status = OrderStatus.PENDING;
        order.couponId = couponId;

        /* order 무작위 번호 생성 */
        order.orderNo = createOrderNumber();
        return order;
    }

    /* 상품 가격 계산 */
    public void calculateTotalPrice() {
        if (this.orderProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        this.totalPrice = this.orderProducts.stream()
                .map(OrderProduct::calculatePrice)  // 각 상품의 금액 계산
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /* 주문 번호 생성 */
    private static String createOrderNumber(){
        // 주문번호 형식: ORD + timestamp(13자리) + random(3자리)
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 1000);
        return String.format("ORD%d%03d", timestamp, random);
    }

    /* 주문 상태 확인 */
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

    /* 주문 완료 처리 */
    public void complete() {
        this.status = this.status.transitionTo(OrderStatus.COMPLETED);
        this.isSettleable = true;
    }

    /* 주문 롤백 처리 */
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

    /* 재고 확인 */
    public StockValidationResult validateStock() {
        if (this.orderProducts.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND, "주문 상품이 없습니다");
        }
        
        List<InsufficientStockItem> insufficientItems = new ArrayList<>();
        
        for (OrderProduct orderProduct : this.orderProducts) {
            if (!orderProduct.hasEnoughStock()) {
                insufficientItems.add(new InsufficientStockItem(
                    orderProduct.getProductOption().getProductOptionId(),
                    orderProduct.getProductQuantity(),
                    orderProduct.getAvailableStock()
                ));
            }
        }
        
        if (!insufficientItems.isEmpty()) {
            return StockValidationResult.invalid(insufficientItems);
        }
        
        return StockValidationResult.valid();
    }
}
