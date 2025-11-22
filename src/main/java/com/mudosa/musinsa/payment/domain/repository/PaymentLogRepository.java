package com.mudosa.musinsa.payment.domain.repository;

import com.mudosa.musinsa.payment.domain.model.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
    PaymentLog findByPaymentId(Long payment_id);

    List<PaymentLog> findAllByPaymentId(Long id);
}
