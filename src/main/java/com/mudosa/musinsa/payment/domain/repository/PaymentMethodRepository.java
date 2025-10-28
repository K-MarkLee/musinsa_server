package com.mudosa.musinsa.payment.domain.repository;

import com.mudosa.musinsa.payment.domain.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    
    List<PaymentMethod> findByIsActiveTrue();
    
    Optional<PaymentMethod> findByPaymentName(String paymentName);
}
