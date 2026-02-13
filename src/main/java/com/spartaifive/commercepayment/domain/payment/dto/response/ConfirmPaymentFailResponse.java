package com.spartaifive.commercepayment.domain.payment.dto.response;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmPaymentFailResponse(
        Long paymentId,
        Long orderId,
        BigDecimal actualAmount,
        String status,
        String failReason,
        String portonePaymentId,
        LocalDateTime paidAt
) implements ConfirmPaymentResponse {
    public static ConfirmPaymentFailResponse from(Payment payment, String failReason) {
        return new ConfirmPaymentFailResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getActualAmount(),
                payment.getPaymentStatus().getStatusCode(),
                failReason,
                payment.getPortonePaymentId(),
                payment.getPaidAt()
        );
    }
}
