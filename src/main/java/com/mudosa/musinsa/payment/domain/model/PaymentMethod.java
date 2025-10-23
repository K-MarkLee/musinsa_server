package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 수단 테이블
 * - 공통 참조 데이터
 */
@Entity
@Table(name = "payment_method")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    private Integer id;
    
    @Column(name = "payment_name", nullable = false, length = 50, unique = true)
    private String paymentName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * 결제 수단 생성t
     */
    public static PaymentMethod create(String paymentName) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.paymentName = paymentName;
        paymentMethod.isActive = true;
        return paymentMethod;
    }

    /**
     * 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}
