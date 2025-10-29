package com.mudosa.musinsa.payment.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

    public static PaymentMethod create(String paymentName) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.paymentName = paymentName;
        paymentMethod.isActive = true;
        return paymentMethod;
    }

    /* 유효한 결제수단인지 확인 */
    public void validateActive() {
        if (!this.isActive) {
            throw new IllegalStateException(
                    String.format("사용 불가능한 결제 수단입니다: %s", this.paymentName)
            );
        }
    }
}
