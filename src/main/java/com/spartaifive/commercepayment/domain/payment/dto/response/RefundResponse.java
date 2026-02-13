package com.spartaifive.commercepayment.domain.payment.dto.response;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse (
        Long orderId,
        Long paymentId,
        String portonePaymentId,
        String paymentStatus,
        String orderStatus,
        BigDecimal cancelledAmount,
        LocalDateTime refundedAt
){
    public static RefundResponse from(Payment payment, Order order) {
        return new RefundResponse(
                order.getId(),
                payment.getId(),
                payment.getPortonePaymentId(),
                payment.getPaymentStatus().getStatusCode(),
                order.getStatus().getStatusCode(),
                payment.getActualAmount(),
                payment.getRefundedAt()
        );
    }
}
