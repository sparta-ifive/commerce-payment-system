package com.spartaifive.commercepayment.domain.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class ConfirmedPaymentAndUser {
    private final Long userId;
    private final BigDecimal confirmedPayAmount;
}
