package com.spartaifive.commercepayment.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentAttemptRequest(
        @NotNull
        Long orderId
) {
}
