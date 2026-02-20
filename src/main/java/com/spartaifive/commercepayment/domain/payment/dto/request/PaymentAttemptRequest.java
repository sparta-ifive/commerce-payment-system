package com.spartaifive.commercepayment.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentAttemptRequest(
        @NotNull(message = "주문 ID는 필수 입니다.")
        Long orderId,
//        ,String clientRequestId

        @Min(0) // optional
        BigDecimal pointsToUse
) {
}
