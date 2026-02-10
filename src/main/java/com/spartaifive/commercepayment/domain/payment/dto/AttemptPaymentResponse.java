package com.spartaifive.commercepayment.domain.payment.dto;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class AttemptPaymentResponse {
    private final Long id;
    private final String paymentId;
    private final PaymentStatus status;
    private final BigDecimal payAmount;

    public static AttemptPaymentResponse of(Payment payment) {
        return new AttemptPaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getPayAmount()
        );
    }
}

