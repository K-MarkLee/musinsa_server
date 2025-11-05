package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.coupon.domain.service.MemberCouponService;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.application.dto.*;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import com.mudosa.musinsa.order.domain.model.Orders;
import com.mudosa.musinsa.order.domain.model.StockValidationResult;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.order.domain.service.StockValidator;
import com.mudosa.musinsa.payment.application.dto.OrderValidationResult;
import com.mudosa.musinsa.payment.application.dto.PaymentDetailDto;
import com.mudosa.musinsa.payment.application.service.PaymentFetchService;
import com.mudosa.musinsa.product.application.CartService;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.model.ProductOptionValue;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MemberCouponService memberCouponService;
    private final ProductOptionRepository productOptionRepository;
    private final StockValidator stockValidator;
    private final UserRepository userRepository;
    private final PaymentFetchService paymentService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeOrder(Long orderId) {
        log.info("주문 완료 처리 시작 - orderId: {}", orderId);

        /* 주문 조회 및 검증 */
        Orders orders = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        /* 주문 상태 재검증 -> 동시성 제어를 위해 */
        orders.validatePending();

        /* 주문 상품 재검증 */
        orders.validateOrderProducts();

        log.info("주문 검증 완료 - orderId: {}, orderProducts: {}",
                orderId, orders.getOrderProducts().size());

        /* 재고 차감 */
        orders.decreaseStock();
        log.info("재고 차감 완료 - orderId: {}", orderId);

        /* 주문 상태 변경 */
        orders.complete();
        orderRepository.save(orders);

        log.info("주문 상태 변경 완료 - orderId: {}, status: COMPLETED", orderId);

        /* 주문 제품에 대한 장바구니 제품 삭제 */
        deleteCartItems(orders);

        log.info("장바구니 삭제 완료 - orderId: {}, userId: {}", orderId, orders.getUser().getId());

        /* 쿠폰을 사용한 주문이라면 MemberCoupon 사용 처리 */
        useCouponIfExists(orders);

        log.info("주문 완료 처리 성공 - orderId: {}", orderId);
    }

    private void deleteCartItems(Orders order) {
        List<Long> productOptionIds = order.getOrderProducts().stream()
                .map(op -> op.getProductOption().getProductOptionId())
                .toList();

        cartService.deleteCartItemsByProductOptions(order.getUser().getId(), productOptionIds);

        log.info("장바구니 삭제 완료 - orderId: {}, count: {}",
                order.getId(), productOptionIds.size());
    }

    private void useCouponIfExists(Orders order) {
        if (!order.hasCoupon()) {
            return;
        }

        try {
            memberCouponService.useMemberCoupon(
                    order.getUser().getId(),
                    order.getCouponId(),
                    order.getId()
            );

            log.info("쿠폰 사용 처리 완료 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId());

        } catch (Exception e) {
            log.error("쿠폰 사용 처리 실패 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackOrder(Long orderId) {
        log.warn("주문 롤백 시작 - orderId: {}", orderId);

        /* 재고 복구 */
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        orders.restoreStock();
        log.info("재고 복구 완료 - orderId: {}", orderId);

        /* 주문 상태 복구 */
        orders.rollback();
        orderRepository.save(orders);

        /* 쿠폰 복구 */
        rollbackCouponIfUsed(orders);

        log.warn("주문 롤백 완료 - orderId: {}, status: PENDING", orderId);
    }

    public boolean isOrderCompleted(Long orderId) {
        return orderRepository.findById(orderId).map(order -> order.getStatus().isCompleted()).orElse(false);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderValidationResult validateAndPrepareOrder(
            String orderNo,
            Long userId,
            BigDecimal requestAmount,
            Long couponId) {

        log.info("주문 검증 시작 - orderNo: {}, userId: {}, requestAmount: {}",
                orderNo, userId, requestAmount);

        // 1. 주문 조회
        Orders order = orderRepository.findByOrderNoWithOrderProducts(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                        "주문을 찾을 수 없습니다: " + orderNo));

        // 2. 사용자 권한 검증
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER,
                    "본인의 주문만 결제할 수 있습니다");
        }

        // 3. 주문 상태 검증
        order.validatePending();

        // 4. 재고 검증
        StockValidationResult stockValidationResult = stockValidator.validateStockForOrder(order);
        if (stockValidationResult.hasInsufficientStock()) {
            log.warn("재고 부족 - orderNo: {}", orderNo);
            return OrderValidationResult.insufficientStock(
                    order.getId(),
                    stockValidationResult.getInsufficientItems()
            );
        }
        //5. 적용한 쿠폰 삽입
        order.addCoupon(couponId);

        // 5. 쿠폰 적용 및 최종 금액 계산
        BigDecimal discount = calculateDiscount(order, userId);
        BigDecimal finalAmount = order.getTotalPrice().subtract(discount);

        // 6. 주문에 할인 금액 반영
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            order.applyDiscount(discount);
            orderRepository.save(order);
            log.info("할인 적용 완료 - orderId: {}, discount: {}", order.getId(), discount);
        }


        log.info("주문 검증 완료 - orderId: {}, finalAmount: {}", order.getId(), finalAmount);

        return OrderValidationResult.success(
                order.getId(),
                order.getUser().getId(),
                finalAmount,
                discount
        );
    }

    /* 쿠폰 할인 계산 */
    private BigDecimal calculateDiscount(Orders order, Long userId) {
        if (!order.hasCoupon()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal discount = memberCouponService.calculateDiscount(
                    userId,
                    order.getCouponId(),
                    order.getTotalPrice()
            );

            log.info("쿠폰 할인 계산 완료 - orderId: {}, couponId: {}, discount: {}",
                    order.getId(), order.getCouponId(), discount);

            return discount;

        } catch (Exception e) {
            log.error("쿠폰 할인 계산 실패 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId(), e);
            throw new BusinessException(ErrorCode.COUPON_NOT_FOUND,
                    "쿠폰 적용에 실패했습니다: " + e.getMessage());
        }
    }

    private void rollbackCouponIfUsed(Orders order) {
        if (!order.hasCoupon()) {
            log.debug("쿠폰 없음 - 쿠폰 롤백 스킵");
            return;
        }

        try {
            memberCouponService.rollbackMemberCoupon(
                    order.getUser().getId(),
                    order.getCouponId(),
                    order.getId()
            );

            log.info("✓ 쿠폰 복구 완료 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId());

        } catch (Exception e) {
            log.error("쿠폰 복구 실패 - orderId: {}, couponId: {}",
                    order.getId(), order.getCouponId(), e);

            // TODO:쿠폰 복구 못했다고 결제를 취소해야되나...?
            throw new BusinessException(
                    ErrorCode.COUPON_ROLLBACK_INVALID,
                    "쿠폰 복구에 실패했습니다: " + e.getMessage()
            );
        }
    }

    @Transactional
    public OrderCreateResponse createPendingOrder(OrderCreateRequest request, Long userId) {
        log.info("주문 생성 시작 - userId: {}, itemCount: {}",
                userId, request.getItems().size());

        /* 1. 사용자 조회 */
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        /* 2. 상품 옵션 조회 */
        List<Long> productOptionIds = request.getItems().stream()
                .map(OrderCreateItem::getProductOptionId)
                .toList();

        List<ProductOption> productOptions = productOptionRepository.findAllByIdWithInventory(productOptionIds);

        // 요청된 모든 상품 옵션이 존재하는지 확인
        if (productOptions.size() != productOptionIds.size()) {
            List<Long> foundIds = productOptions.stream()
                    .map(ProductOption::getProductOptionId)
                    .toList();
            List<Long> notFoundIds = productOptionIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            log.error("존재하지 않는 상품 옵션: {}", notFoundIds);
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND,
                    "존재하지 않는 상품 옵션: " + notFoundIds);
        }

        /* 3. 총 금액 계산 */
        BigDecimal totalPrice = calculateTotalPrice(request.getItems(), productOptions);
        log.info("총 금액 계산 완료 - totalPrice: {}", totalPrice);

        /* 4. 주문 생성 (status: PENDING) */
        Orders order = Orders.create(
                totalPrice,
                user,  // User 엔티티 전달
                request.getCouponId()
        );

        /* 5. 주문 아이템 생성 */
        List<OrderProduct> orderProducts = createOrderProducts(
                request.getItems(),
                productOptions,
                userId
        );

        order.addOrderProducts(orderProducts);

        /* 6. 재고 확인 (StockValidator 재사용) */
        StockValidationResult stockValidation = stockValidator.validateStockForOrder(order);
        if (stockValidation.hasInsufficientStock()) {
            log.warn("재고 부족 - 부족한 상품 수: {}", stockValidation.getInsufficientItems().size());
            return OrderCreateResponse.insufficientStock(stockValidation.getInsufficientItems());
        }

        /* 7. 주문 저장 */
        Orders savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 - orderId: {}, orderNo: {}, totalPrice: {}",
                savedOrder.getId(), savedOrder.getOrderNo(), savedOrder.getTotalPrice());

        return OrderCreateResponse.success(savedOrder.getId(), savedOrder.getOrderNo());
    }

    private BigDecimal calculateTotalPrice(
            List<OrderCreateItem> items,
            List<ProductOption> productOptions) {

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderCreateItem item : items) {
            ProductOption productOption = productOptions.stream()
                    .filter(po -> po.getProductOptionId().equals(item.getProductOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

            BigDecimal itemPrice = productOption.getProductPrice().getAmount()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);
        }

        return totalPrice;
    }

    private List<OrderProduct> createOrderProducts(
            List<OrderCreateItem> items,
            List<ProductOption> productOptions,
            Long userId) {

        List<OrderProduct> orderProducts = new ArrayList<>();

        for (OrderCreateItem item : items) {
            ProductOption productOption = productOptions.stream()
                    .filter(po -> po.getProductOptionId().equals(item.getProductOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

            OrderProduct orderProduct = OrderProduct.create(
                    userId,
                    productOption,
                    productOption.getProductPrice().getAmount(),
                    item.getQuantity(),
                    null,  // event
                    null,  // eventOption
                    null   // limitScope
            );

            orderProducts.add(orderProduct);
        }

        return orderProducts;
    }

    @Transactional(readOnly = true)
    public PendingOrderResponse fetchPendingOrder(String orderNo) {
        log.info("[Order] 주문서 조회 시작 - orderNo: {}", orderNo);

        Orders orders = orderRepository.findByOrderNoWithUserAndProducts(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        /* 주문 상태 확인 */
        orders.validatePending();

        User orderUser = orders.getUser();
        log.info("[Order] 사용자 정보 조회 완료 - userId: {}, userName: {}",
                orderUser.getId(), orderUser.getUserName());

        List<Long> productOptionIds = orders.getOrderProducts()
                .stream()
                .map(op -> op.getProductOption().getProductOptionId())
                .toList();

        orderRepository.findProductOptionsWithValues(productOptionIds);

        log.info("[Order] ProductOption 옵션 값 조회 완료 - productOptionIds: {}", productOptionIds);

        List<PendingOrderItem> orderProducts = orders.getOrderProducts()
                .stream()
                .map(op -> {
                    ProductOption productOption = op.getProductOption();
                    List<ProductOptionValue> optionValues = productOption.getProductOptionValues();

                    // SIZE 옵션 추출
                    String sizeValue = optionValues.stream()
                            .filter(pov -> "SIZE".equals(pov.getOptionValue().getOptionName()))
                            .map(pov -> pov.getOptionValue().getOptionValue())
                            .findFirst()
                            .orElse("");

                    // COLOR 옵션 추출
                    String colorValue = optionValues.stream()
                            .filter(pov -> "COLOR".equals(pov.getOptionValue().getOptionName()))
                            .map(pov -> pov.getOptionValue().getOptionValue())
                            .findFirst()
                            .orElse("");

                    //imageUrl
                    String imageUrl = op.getProductOption().getProduct().getImages().stream()
                            .filter(image -> image.getIsThumbnail()).map(i-> i.getImageUrl()).findFirst().orElse("");

                    return PendingOrderItem.builder()
                            .productOptionId(op.getProductOptionId())
                            .productOptionName(productOption.getProduct().getProductName())
                            .amount(op.getProductPrice())
                            .quantity(op.getProductQuantity())
                            .brandName(productOption.getProduct().getBrand().getNameKo())
                            .size(sizeValue)
                            .imageUrl(imageUrl)
                            .color(colorValue)
                            .build();
                })
                .toList();

        log.info("[Order] 주문 상품 변환 완료 - 상품 수: {}", orderProducts.size());

        /* [4단계] 쿠폰 조회 */
        List<OrderMemberCoupon> coupons = memberCouponService.findMemberCoupons(orderUser.getId());
        log.info("[Order] 쿠폰 조회 완료 - 쿠폰 수: {}", coupons.size());

        /* [5단계] 응답 생성 */
        PendingOrderResponse response = PendingOrderResponse.builder()
                .orderNo(orderNo)
                .userContactNumber(orderUser.getContactNumber())
                .userName(orderUser.getUserName())
                .userAddress(orderUser.getCurrentAddress())
                .orderProducts(orderProducts)
                .coupons(coupons)
                .build();

        log.info("[Order] 주문서 조회 완료 - orderNo: {}, 상품 수: {}, 쿠폰 수: {}",
                orderNo, orderProducts.size(), coupons.size());

        return response;
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse fetchOrderDetail(String orderNo) {
        log.info("[Order] 주문 상세 조회 시작 - orderNo: {}", orderNo);

        Orders orders = orderRepository.findByOrderNoWithUserAndProducts(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        User orderUser = orders.getUser();
        log.info("[Order] 주문 상태: {}, 사용자: {}", orders.getStatus(), orderUser.getUserName());

        List<Long> productOptionIds = orders.getOrderProducts()
                .stream()
                .map(op -> op.getProductOption().getProductOptionId())
                .toList();


        // 주문 상품 변환
        List<OrderDetailItem> orderProducts = orders.getOrderProducts()
                .stream()
                .map(op -> {
                    ProductOption productOption = op.getProductOption();
                    List<ProductOptionValue> optionValues = productOption.getProductOptionValues();

                    // SIZE 옵션 추출
                    String sizeValue = optionValues.stream()
                            .filter(pov -> "SIZE".equals(pov.getOptionValue().getOptionName()))
                            .map(pov -> pov.getOptionValue().getOptionValue())
                            .findFirst()
                            .orElse("");

                    // COLOR 옵션 추출
                    String colorValue = optionValues.stream()
                            .filter(pov -> "COLOR".equals(pov.getOptionValue().getOptionName()))
                            .map(pov -> pov.getOptionValue().getOptionValue())
                            .findFirst()
                            .orElse("");

                    // imageUrl
                    String imageUrl = op.getProductOption().getProduct().getImages().stream()
                            .filter(image -> image.getIsThumbnail())
                            .map(i -> i.getImageUrl())
                            .findFirst()
                            .orElse("");

                    return OrderDetailItem.builder()
                            .productOptionId(op.getProductOptionId())
                            .productOptionName(productOption.getProduct().getProductName())
                            .amount(op.getProductPrice())
                            .quantity(op.getProductQuantity())
                            .brandName(productOption.getProduct().getBrand().getNameKo())
                            .size(sizeValue)
                            .color(colorValue)
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();

        // 금액 계산
        BigDecimal discountAmount = orders.getTotalDiscount();
        BigDecimal totalProductsAmount = orders.getTotalPrice();

        //결제 내역
        PaymentDetailDto paymentDto = paymentService.fetchPaymentDetail(orders.getId());

        // 응답 생성
        OrderDetailResponse response = OrderDetailResponse.builder()
                .orderNo(orders.getOrderNo())
                .orderStatus(orders.getStatus().name())
                .userName(orderUser.getUserName())
                .userAddress(orderUser.getCurrentAddress())
                .userContactNumber(orderUser.getContactNumber())
                .orderedAt(orders.getCreatedAt())
                .completedAt(orders.getUpdatedAt())
                .orderProducts(orderProducts)
                .totalProductAmount(paymentDto.getTotalAmount())
                .discountAmount(discountAmount)
                .totalProductAmount(totalProductsAmount)
                .paymentFinalAmount(paymentDto.getTotalAmount())
                .paymentMethod(paymentDto.getMethod())
                .approvedAt(paymentDto.getApprovedAt())
                .pgProvider(paymentDto.getPgProvider())
                .paymentStatus(paymentDto.getPaymentStatus())
                .build();

        log.info("[Order] 주문 상세 조회 완료 - orderNo: {}, 상품 수: {}",
                orderNo, orderProducts.size());

        return response;
    }


}
