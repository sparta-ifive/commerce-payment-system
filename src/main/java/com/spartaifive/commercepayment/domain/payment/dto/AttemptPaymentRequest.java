package com.spartaifive.commercepayment.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AttemptPaymentRequest {
    // TODO: bean validation 추가
    private Long orderId;
    private BigDecimal totalPrice;
}
