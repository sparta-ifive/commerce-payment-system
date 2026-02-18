package com.spartaifive.commercepayment.domain.user.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentUserResponse {
    private String customerUid;
    private String email;
    private String name;
    private String phone;
    private BigDecimal pointsReadyToSpend;
    private BigDecimal pointsNotReadyToSpend;
}
