package com.mudosa.musinsa.payment.application.dto.request;

import com.mudosa.musinsa.payment.domain.model.PgProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCancelRequest {
    @NotBlank(message = "취소 이유는 필수값입니다.")
    private final String cancelReason;

    @NotBlank(message = "PaymentKey값은 필수입니다")
    private final String paymentTransactionId;

    private PgProvider pgProvider;
}
