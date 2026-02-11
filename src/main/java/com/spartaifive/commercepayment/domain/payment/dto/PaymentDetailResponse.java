package com.spartaifive.commercepayment.domain.payment.dto;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDetailResponse (
        Long paymentId,
        Long orderId,
        String merchantPaymentId,
        String portonePaymentId,
        String status,
        BigDecimal expectedAmount,
        BigDecimal actualAmount,
        LocalDateTime attemptedAt,
        LocalDateTime paidAt,
        LocalDateTime failedAt,
        LocalDateTime refundedAt
){
    public static PaymentDetailResponse from(Payment payment) {
        return new PaymentDetailResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getMerchantPaymentId(),
                payment.getPortonePaymentId(),
                payment.getPaymentStatus().getStatusCode(),
                payment.getExpectedAmount(),
                payment.getActualAmount(),
                payment.getAttemptedAt(),
                payment.getPaidAt(),
                payment.getFailedAt(),
                payment.getRefundedAt()
        );
    }
}
