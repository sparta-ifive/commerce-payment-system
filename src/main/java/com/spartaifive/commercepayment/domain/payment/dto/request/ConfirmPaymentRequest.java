package com.spartaifive.commercepayment.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
        @NotNull(message = "merchantPayment ID는 필수입니다")
        @JsonAlias({"portonePaymentId", "paymentId"})
        String merchantPaymentId,
        @NotNull(message = "주문 ID는 필수입니다")
        Long orderId
) {
}
