package com.mudosa.musinsa.settlement.application;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import com.mudosa.musinsa.order.domain.model.Order;
import com.mudosa.musinsa.order.domain.repository.OrderRepository;
import com.mudosa.musinsa.payment.application.event.PaymentApprovedEvent;
import com.mudosa.musinsa.payment.domain.model.Payment;
import com.mudosa.musinsa.payment.domain.repository.PaymentRepository;
import com.mudosa.musinsa.settlement.domain.model.TransactionType;
import com.mudosa.musinsa.settlement.domain.service.PgFeeCalculator;
import com.mudosa.musinsa.settlement.domain.vo.CommissionRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 결제-정산 연동 서비스
 *
 * 트리거: PaymentApprovedEvent (결제 승인 이벤트)
 * 결제가 완료될 때마다 자동 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSettlementService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final BrandRepository brandRepository;
    private final SettlementApplicationService settlementApplicationService;
    private final PgFeeCalculator pgFeeCalculator;

    //TODO: Async학습 !! 비동기를 이렇게 쓰면 문제가 생긴다
    @Async
    @EventListener
    public void onPaymentApproved(PaymentApprovedEvent event) {
        log.info("PaymentApprovedEvent received - paymentId={}, pgTxId={}",
                event.getPaymentId(), event.getPgTransactionId());

        try {
            createSettlementsForPayment(event.getPaymentId(), event.getPgTransactionId());
        } catch (Exception e) {
            log.error("Settlement 생성 실패 - 수동 처리 필요, paymentId={}", event.getPaymentId(), e);
            // TODO: 재시도 큐에 추가 또는 관리자 알림 로직 구현
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSettlementsForPayment(Long paymentId, String pgTransactionId) {
        log.info("→ Settlement 생성 트랜잭션 시작 - paymentId={}", paymentId);

        if (settlementApplicationService.existsByPaymentId(paymentId)) {
            log.warn("Settlement이 이미 존재함 - 중복 생성 방지, paymentId={}", paymentId);
            return;
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderRepository.findByIdWithProductsAndBrand(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            createSettlementForOrderProduct(payment, orderProduct, pgTransactionId);
        }

        log.info("← Settlement 생성 트랜잭션 커밋 완료 - paymentId={}, 생성 건수={}",
                paymentId, order.getOrderProducts().size());
    }

    private void createSettlementForOrderProduct(
            Payment payment,
            OrderProduct orderProduct,
            String pgTransactionId) {

        Long brandId = orderProduct.getProductOption().getProduct().getBrand().getBrandId();

        Money transactionAmount = orderProduct.getProductPrice()
                .multiply(orderProduct.getProductQuantity());

        Money pgFeeAmount = pgFeeCalculator.calculate(payment.getMethod(), transactionAmount);

        BigDecimal commissionRate = getBrandCommissionRate(brandId);

        String timezone = "Asia/Seoul";

        //TODO: 메서드 분리가 필요할까?
        settlementApplicationService.createSettlementTransaction(
                brandId,
                payment.getId(),
                pgTransactionId,
                transactionAmount,
                commissionRate,
                pgFeeAmount,
                TransactionType.ORDER,
                timezone
        );

        log.info("Settlement 생성 완료 - brandId={}, amount={}, pgFee={}",
                brandId, transactionAmount, pgFeeAmount);
    }

    private BigDecimal getBrandCommissionRate(Long brandId) {
        return brandRepository.findById(brandId)
                .map(Brand::getCommissionRate)
                .orElse(CommissionRate.getDefaultRate());
    }
}