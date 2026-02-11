package com.spartaifive.commercepayment.domain.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConfirmPaymentResponse {
    private final Long paymentId;
    private final String portOnePaymentId;
    private final Long orderId;
}
