package com.mudosa.musinsa.payment.domain.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.mudosa.musinsa.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
}
