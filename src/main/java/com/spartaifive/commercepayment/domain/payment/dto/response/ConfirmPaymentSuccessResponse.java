package com.spartaifive.commercepayment.domain.payment.dto.response;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmPaymentSuccessResponse(
        Long paymentId,
        Long orderId,
        BigDecimal actualAmount,
        String status,
        String portonePaymentId,
        LocalDateTime paidAt
) implements ConfirmPaymentResponse {
    public static ConfirmPaymentSuccessResponse from(Payment payment) {
        return new ConfirmPaymentSuccessResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getActualAmount(),
                payment.getPaymentStatus().getStatusCode(),
                payment.getPortonePaymentId(),
                payment.getPaidAt()
        );
    }
}
