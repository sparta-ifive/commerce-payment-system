package com.spartaifive.commercepayment.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ConfirmPaymentRequest {
    // TODO: bean validation 추가
    private Long paymentId;
    private String portOnePaymentId;
    private Long orderId;
}
