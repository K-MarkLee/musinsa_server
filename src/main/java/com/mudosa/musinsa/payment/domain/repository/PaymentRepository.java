package com.mudosa.musinsa.payment.domain.repository;

import com.mudosa.musinsa.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Payment Repository
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrderId(Long orderId);
    
    Optional<Payment> findByPgTransactionId(String pgTransactionId);
}
