# DDD 기반 주문-결제 도메인 설계

## 📋 목차
1. [도메인 이해](#도메인-이해)
2. [유비쿼터스 언어](#유비쿼터스-언어)
3. [바운디드 컨텍스트](#바운디드-컨텍스트)
4. [도메인 모델 설계](#도메인-모델-설계)
5. [애그리거트 설계](#애그리거트-설계)
6. [도메인 이벤트](#도메인-이벤트)
7. [아키텍처 레이어](#아키텍처-레이어)

---

## 🎯 도메인 이해

### 비즈니스 시나리오

**무신사 쇼핑몰의 주문-결제 프로세스:**

```
1. 사용자가 장바구니에서 상품 선택
2. 주문 생성 (여러 상품 + 옵션)
   - 쿠폰 적용 가능
   - 할인 금액 계산
3. 결제 진행
   - 결제 수단 선택
   - PG사 연동
4. 결제 완료
   - 주문 상태 변경
   - 재고 차감
5. 정산
   - 브랜드별 정산
```

### 핵심 도메인 규칙

1. **주문 생성 규칙**
   - 하나의 주문은 여러 상품을 포함할 수 있다
   - 각 상품은 특정 옵션을 가져야 한다
   - 쿠폰은 주문 단위로 적용된다
   - 총 주문 금액 = Σ(상품 가격 × 수량) - 할인 금액

2. **결제 규칙**
   - 하나의 주문은 하나의 결제를 가진다
   - 결제는 승인/취소/실패 상태를 가진다
   - 결제 취소 시 부분 취소 가능
   - 결제 내역은 모두 로그로 남긴다

3. **정산 규칙**
   - 결제 완료 후 정산 가능 상태로 변경
   - 브랜드별로 정산 금액 계산
   - 정산 완료 후 변경 불가

---

## 💬 유비쿼터스 언어 (Ubiquitous Language)

### 주문 도메인 용어

| 용어 | 영문 | 설명 |
|------|------|------|
| **주문** | Order | 고객이 상품을 구매하기 위한 요청 |
| **주문번호** | OrderNo | 외부에 노출되는 주문 식별자 |
| **주문자** | Orderer | 주문을 생성한 사용자 |
| **주문상품** | OrderProduct | 주문에 포함된 개별 상품 정보 |
| **주문금액** | OrderAmount | 주문의 금액 정보 (총액, 할인, 최종) |
| **주문상태** | OrderStatus | 주문의 현재 상태 (생성, 결제완료, 배송중 등) |

### 결제 도메인 용어

| 용어 | 영문 | 설명 |
|------|------|------|
| **결제** | Payment | 주문에 대한 금액 지불 |
| **결제수단** | PaymentMethod | 결제 방법 (카드, 계좌이체 등) |
| **결제상태** | PaymentStatus | 결제의 현재 상태 (대기, 승인, 취소 등) |
| **결제금액** | PaymentAmount | 실제 결제된 금액 |
| **PG거래ID** | PgTransactionId | PG사에서 부여한 거래 식별자 |
| **결제로그** | PaymentLog | 결제 과정의 모든 이벤트 기록 |

### 정산 도메인 용어

| 용어 | 영문 | 설명 |
|------|------|------|
| **정산** | Settlement | 브랜드에 지급할 금액 계산 |
| **정산가능여부** | IsSettleable | 정산 가능한 상태인지 여부 |
| **정산일시** | SettledAt | 실제 정산이 완료된 시점 |

---

## 🏛️ 바운디드 컨텍스트 (Bounded Context)

```
┌─────────────────────────────────────────────────────────────┐
│                     Order-Payment Context                    │
│                    (주문-결제 컨텍스트)                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐        ┌──────────────────┐          │
│  │  Order Aggregate │◄──────►│ Payment Aggregate │          │
│  │   (주문 집합)      │        │  (결제 집합)       │          │
│  └──────────────────┘        └──────────────────┘          │
│           │                           │                      │
│           │                           │                      │
│           ▼                           ▼                      │
│  ┌──────────────────┐        ┌──────────────────┐          │
│  │  OrderProduct    │        │   PaymentLog     │          │
│  │   (주문상품)       │        │   (결제로그)      │          │
│  └──────────────────┘        └──────────────────┘          │
│                                                               │
└─────────────────────────────────────────────────────────────┘
         │                              │
         │ 참조                          │ 참조
         │                              │
┌────────▼──────────┐          ┌───────▼──────────┐
│  User Context      │          │  Product Context  │
│  (회원 컨텍스트)     │          │  (상품 컨텍스트)    │
└────────────────────┘          └───────────────────┘
         │                              │
         │ 참조                          │ 참조
         │                              │
┌────────▼──────────┐          ┌───────▼──────────┐
│  Coupon Context    │          │  Brand Context    │
│  (쿠폰 컨텍스트)     │          │  (브랜드 컨텍스트)  │
└────────────────────┘          └───────────────────┘
```

### 컨텍스트 관계

- **Order-Payment**: 강한 일관성 (동일 트랜잭션)
- **Order ↔ User**: 약한 참조 (ID만 보유)
- **Order ↔ Product**: 약한 참조 (ID만 보유)
- **Order ↔ Coupon**: 약한 참조 (ID만 보유)
- **Order ↔ Brand**: 약한 참조 (ID만 보유)

---

## 🎨 도메인 모델 설계

### 1. Order Aggregate (주문 애그리거트)

```java
/**
 * 주문 애그리거트 루트
 * - 주문 생성, 취소, 상태 변경
 * - 주문 금액 계산
 * - 비즈니스 규칙 검증
 */
@Entity
@Table(name = "`Order`")
@Getter
public class Order extends BaseEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    
    // Value Object: 주문자 정보
    @Embedded
    private Orderer orderer;
    
    // Value Object: 주문 금액
    @Embedded
    private OrderAmount orderAmount;
    
    // Value Object: 주문 상태
    @Embedded
    private OrderStatus orderStatus;
    
    // 외부 노출용 주문번호 (Value Object)
    @Embedded
    private OrderNo orderNo;
    
    // 쿠폰 (약한 참조)
    private Long couponId;
    
    // 브랜드 (약한 참조)
    private Long brandId;
    
    // 주문 상품 목록 (Entity)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();
    
    // 정산 정보
    private boolean isSettleable;
    private LocalDateTime settledAt;
    
    // 생성자 (팩토리 메서드)
    protected Order() {}
    
    /**
     * 주문 생성 (정적 팩토리 메서드)
     */
    public static Order create(
        Orderer orderer,
        List<OrderProductCommand> productCommands,
        Long couponId,
        Long brandId
    ) {
        Order order = new Order();
        order.orderer = orderer;
        order.couponId = couponId;
        order.brandId = brandId;
        order.orderNo = OrderNo.generate();
        order.orderStatus = OrderStatus.CREATED;
        
        // 주문 상품 추가
        productCommands.forEach(cmd -> 
            order.addOrderProduct(cmd.toOrderProduct())
        );
        
        // 주문 금액 계산
        order.calculateAmount();
        
        // 도메인 이벤트 발행
        order.registerEvent(new OrderCreatedEvent(order));
        
        return order;
    }
    
    /**
     * 주문 상품 추가
     */
    private void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
        orderProduct.assignOrder(this);
    }
    
    /**
     * 주문 금액 계산 (핵심 비즈니스 로직)
     */
    private void calculateAmount() {
        BigDecimal totalPrice = orderProducts.stream()
            .map(OrderProduct::calculatePrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discount = calculateDiscount(totalPrice);
        BigDecimal finalAmount = totalPrice.subtract(discount);
        
        this.orderAmount = new OrderAmount(totalPrice, discount, finalAmount);
    }
    
    /**
     * 할인 금액 계산 (쿠폰 적용)
     */
    private BigDecimal calculateDiscount(BigDecimal totalPrice) {
        // TODO: 쿠폰 서비스 호출하여 할인 금액 계산
        return BigDecimal.ZERO;
    }
    
    /**
     * 결제 완료 처리
     */
    public void completePayment() {
        validateCanCompletePayment();
        
        this.orderStatus = OrderStatus.PAYMENT_COMPLETED;
        this.isSettleable = true;
        
        // 도메인 이벤트 발행
        this.registerEvent(new OrderPaymentCompletedEvent(this));
    }
    
    /**
     * 주문 취소
     */
    public void cancel(String reason) {
        validateCanCancel();
        
        this.orderStatus = OrderStatus.CANCELLED;
        
        // 도메인 이벤트 발행
        this.registerEvent(new OrderCancelledEvent(this, reason));
    }
    
    /**
     * 정산 완료 처리
     */
    public void completeSettlement() {
        if (!this.isSettleable) {
            throw new IllegalStateException("정산 가능한 상태가 아닙니다.");
        }
        
        this.settledAt = LocalDateTime.now();
        this.isSettleable = false;
    }
    
    // 비즈니스 규칙 검증
    private void validateCanCompletePayment() {
        if (this.orderStatus != OrderStatus.CREATED) {
            throw new IllegalStateException("결제 완료 처리할 수 없는 상태입니다.");
        }
    }
    
    private void validateCanCancel() {
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        if (this.orderStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 주문은 취소할 수 없습니다.");
        }
    }
}
```

### 2. OrderProduct Entity (주문상품)

```java
/**
 * 주문 상품 엔티티
 * - Order의 하위 엔티티
 * - 독립적으로 존재할 수 없음 (Order와 생명주기 함께)
 */
@Entity
@Getter
public class OrderProduct extends BaseEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderProductId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    // 주문자 (Order와 동일, 비정규화)
    private Long userId;
    
    // 상품 정보 (약한 참조)
    private Long productId;
    private Long productOptionId;
    
    // 가격 스냅샷 (주문 당시 가격)
    @Embedded
    private Money productPrice;
    
    // 수량
    private int productQuantity;
    
    // 이벤트 정보
    private Long eventId;
    
    // 결제 완료 여부
    private boolean paidFlag;
    
    // 재고 차감 범위
    @Enumerated(EnumType.STRING)
    private LimitScope limitScope;
    
    protected OrderProduct() {}
    
    /**
     * 주문 상품 생성
     */
    public static OrderProduct create(
        Long userId,
        Long productId,
        Long productOptionId,
        Money productPrice,
        int quantity,
        Long eventId
    ) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.userId = userId;
        orderProduct.productId = productId;
        orderProduct.productOptionId = productOptionId;
        orderProduct.productPrice = productPrice;
        orderProduct.productQuantity = quantity;
        orderProduct.eventId = eventId;
        orderProduct.paidFlag = false;
        
        // 비즈니스 규칙 검증
        orderProduct.validate();
        
        return orderProduct;
    }
    
    /**
     * 주문 할당
     */
    void assignOrder(Order order) {
        this.order = order;
    }
    
    /**
     * 가격 계산
     */
    public BigDecimal calculatePrice() {
        return productPrice.multiply(productQuantity);
    }
    
    /**
     * 결제 완료 처리
     */
    public void completePaid() {
        this.paidFlag = true;
    }
    
    /**
     * 비즈니스 규칙 검증
     */
    private void validate() {
        if (productQuantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        if (productPrice.isNegative()) {
            throw new IllegalArgumentException("가격은 음수일 수 없습니다.");
        }
    }
}
```

### 3. Payment Aggregate (결제 애그리거트)

```java
/**
 * 결제 애그리거트 루트
 * - 결제 승인, 취소
 * - PG사 연동
 * - 결제 이력 관리
 */
@Entity
@Getter
public class Payment extends BaseEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    
    // 결제 수단
    private Integer paymentMethodId;
    
    // 결제 상태
    @Embedded
    private PaymentStatus paymentStatus;
    
    // 주문 (약한 참조)
    private Long orderId;
    
    // 결제 금액 정보
    @Embedded
    private PaymentAmount paymentAmount;
    
    // PG사 정보
    @Embedded
    private PgInfo pgInfo;
    
    // 취소 정보
    private LocalDateTime cancelledAt;
    private LocalDateTime approvedAt;
    
    // 결제 로그 (Entity)
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentLog> paymentLogs = new ArrayList<>();
    
    protected Payment() {}
    
    /**
     * 결제 생성 (정적 팩토리 메서드)
     */
    public static Payment create(
        Long orderId,
        Integer paymentMethodId,
        Money amount,
        String pgProvider
    ) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paymentMethodId = paymentMethodId;
        payment.paymentAmount = new PaymentAmount(amount);
        payment.pgInfo = PgInfo.create(pgProvider);
        payment.paymentStatus = PaymentStatus.PENDING;
        
        // 결제 로그 추가
        payment.addLog(PaymentEventType.CREATED, "결제 생성");
        
        // 도메인 이벤트 발행
        payment.registerEvent(new PaymentCreatedEvent(payment));
        
        return payment;
    }
    
    /**
     * 결제 승인
     */
    public void approve(String pgTransactionId) {
        validateCanApprove();
        
        this.pgInfo = pgInfo.withTransactionId(pgTransactionId);
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        
        // 결제 로그 추가
        this.addLog(PaymentEventType.APPROVED, "결제 승인 완료");
        
        // 도메인 이벤트 발행
        this.registerEvent(new PaymentApprovedEvent(this));
    }
    
    /**
     * 결제 취소
     */
    public void cancel(String reason) {
        validateCanCancel();
        
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        
        // 결제 로그 추가
        this.addLog(PaymentEventType.CANCELLED, "결제 취소: " + reason);
        
        // 도메인 이벤트 발행
        this.registerEvent(new PaymentCancelledEvent(this, reason));
    }
    
    /**
     * 결제 실패
     */
    public void fail(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        
        // 결제 로그 추가
        this.addLog(PaymentEventType.FAILED, "결제 실패: " + reason);
        
        // 도메인 이벤트 발행
        this.registerEvent(new PaymentFailedEvent(this, reason));
    }
    
    /**
     * 결제 로그 추가
     */
    private void addLog(PaymentEventType eventType, String description) {
        PaymentLog log = PaymentLog.create(this, eventType, description);
        this.paymentLogs.add(log);
    }
    
    // 비즈니스 규칙 검증
    private void validateCanApprove() {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new IllegalStateException("승인할 수 없는 결제 상태입니다.");
        }
    }
    
    private void validateCanCancel() {
        if (this.paymentStatus == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 결제입니다.");
        }
        if (this.paymentStatus != PaymentStatus.APPROVED) {
            throw new IllegalStateException("승인된 결제만 취소할 수 있습니다.");
        }
    }
}
```

### 4. PaymentLog Entity (결제로그)

```java
/**
 * 결제 로그 엔티티
 * - Payment의 하위 엔티티
 * - 결제 과정의 모든 이벤트 기록
 * - Immutable (변경 불가)
 */
@Entity
@Getter
public class PaymentLog extends BaseEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentHistoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private PaymentEventType eventStatus;
    
    private String description;
    
    protected PaymentLog() {}
    
    /**
     * 결제 로그 생성
     */
    public static PaymentLog create(
        Payment payment,
        PaymentEventType eventType,
        String description
    ) {
        PaymentLog log = new PaymentLog();
        log.payment = payment;
        log.userId = extractUserIdFromPayment(payment);
        log.eventStatus = eventType;
        log.description = description;
        
        return log;
    }
    
    private static Long extractUserIdFromPayment(Payment payment) {
        // TODO: Order 조회하여 userId 추출
        return null;
    }
}
```

---

## 🧩 Value Object 설계

### 1. Orderer (주문자)

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orderer {
    
    @Column(name = "user_id")
    private Integer userId;
    
    public Orderer(Integer userId) {
        validateUserId(userId);
        this.userId = userId;
    }
    
    private void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
    }
}
```

### 2. OrderAmount (주문금액)

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderAmount {
    
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    
    @Column(name = "total_discount")
    private BigDecimal totalDiscount;
    
    @Column(name = "final_payment_amount")
    private BigDecimal finalPaymentAmount;
    
    public OrderAmount(
        BigDecimal totalPrice,
        BigDecimal totalDiscount,
        BigDecimal finalPaymentAmount
    ) {
        validateAmounts(totalPrice, totalDiscount, finalPaymentAmount);
        
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
        this.finalPaymentAmount = finalPaymentAmount;
    }
    
    private void validateAmounts(
        BigDecimal totalPrice,
        BigDecimal totalDiscount,
        BigDecimal finalPaymentAmount
    ) {
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("총 금액은 음수일 수 없습니다.");
        }
        if (totalDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("할인 금액은 음수일 수 없습니다.");
        }
        if (finalPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("최종 금액은 음수일 수 없습니다.");
        }
        if (!totalPrice.subtract(totalDiscount).equals(finalPaymentAmount)) {
            throw new IllegalArgumentException("금액 계산이 올바르지 않습니다.");
        }
    }
}
```

### 3. OrderNo (주문번호)

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderNo {
    
    @Column(name = "order_no", unique = true)
    private String value;
    
    private OrderNo(String value) {
        this.value = value;
    }
    
    /**
     * 주문번호 생성
     * 형식: ORD-{YYYYMMDD}-{6자리 랜덤}
     */
    public static OrderNo generate() {
        String datePart = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = generateRandomString(6);
        
        return new OrderNo("ORD-" + datePart + "-" + randomPart);
    }
    
    private static String generateRandomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length).toUpperCase();
    }
}
```

### 4. Money (금액)

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {
    
    private BigDecimal amount;
    
    public Money(BigDecimal amount) {
        validateAmount(amount);
        this.amount = amount;
    }
    
    public Money(long amount) {
        this(BigDecimal.valueOf(amount));
    }
    
    public static Money ZERO = new Money(BigDecimal.ZERO);
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }
    
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
    }
    
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public BigDecimal multiply(int productQuantity) {
        return this.amount.multiply(BigDecimal.valueOf(productQuantity));
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 null일 수 없습니다.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 음수일 수 없습니다.");
        }
    }
}
```

### 5. PaymentStatus (결제상태)

```java
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentStatus {
    
    @Column(name = "payment_status")
    private Integer statusCodeId;
    
    // 상수
    public static final PaymentStatus PENDING = new PaymentStatus(StatusCode.PAYMENT_PENDING);
    public static final PaymentStatus APPROVED = new PaymentStatus(StatusCode.PAYMENT_APPROVED);
    public static final PaymentStatus CANCELLED = new PaymentStatus(StatusCode.PAYMENT_CANCELLED);
    public static final PaymentStatus FAILED = new PaymentStatus(StatusCode.PAYMENT_FAILED);
    
    private PaymentStatus(StatusCode statusCode) {
        this.statusCodeId = statusCode.getId();
    }
    
    public boolean isPending() {
        return this.equals(PENDING);
    }
    
    public boolean isApproved() {
        return this.equals(APPROVED);
    }
    
    public boolean isCancelled() {
        return this.equals(CANCELLED);
    }
}
```

---

## 🎬 도메인 이벤트

### 주문 이벤트

```java
// 주문 생성
public class OrderCreatedEvent extends DomainEvent {
    private final Long orderId;
    private final Integer userId;
    private final BigDecimal totalAmount;
}

// 주문 결제 완료
public class OrderPaymentCompletedEvent extends DomainEvent {
    private final Long orderId;
    private final LocalDateTime completedAt;
}

// 주문 취소
public class OrderCancelledEvent extends DomainEvent {
    private final Long orderId;
    private final String reason;
}
```

### 결제 이벤트

```java
// 결제 생성
public class PaymentCreatedEvent extends DomainEvent {
    private final Long paymentId;
    private final Long orderId;
}

// 결제 승인
public class PaymentApprovedEvent extends DomainEvent {
    private final Long paymentId;
    private final String pgTransactionId;
}

// 결제 취소
public class PaymentCancelledEvent extends DomainEvent {
    private final Long paymentId;
    private final String reason;
}
```

---

## 📐 아키텍처 레이어

```
┌─────────────────────────────────────────┐
│       Presentation Layer                 │
│   (Controller, DTO, Request/Response)   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Application Layer                  │
│   (ApplicationService, UseCase, Facade) │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Domain Layer                    │
│   (Entity, VO, DomainService, Event)   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Infrastructure Layer                │
│   (Repository, JPA, MyBatis, External)  │
└──────────────────────────────────────────┘
```

---

다음 단계로 실제 코드 구현을 시작하시겠습니까? 🚀
