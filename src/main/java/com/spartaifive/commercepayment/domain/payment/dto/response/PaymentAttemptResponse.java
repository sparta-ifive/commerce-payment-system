package com.spartaifive.commercepayment.domain.payment.dto.response;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;

public record PaymentAttemptResponse(
        Long paymentId,
        String merchantPaymentId,
        Long orderId,
        BigDecimal expectedAmount,
        String status
) {
    public static PaymentAttemptResponse from(Payment payment) {
        return new PaymentAttemptResponse(
                payment.getId(),
                payment.getMerchantPaymentId(),
                payment.getOrder().getId(),
                payment.getExpectedAmount(),
                payment.getPaymentStatus().getStatusCode()
        );
    }
}
