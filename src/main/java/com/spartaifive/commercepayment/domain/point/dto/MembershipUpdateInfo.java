package com.spartaifive.commercepayment.domain.point.dto;

import com.spartaifive.commercepayment.domain.user.entity.User;

import java.math.BigDecimal;

public record MembershipUpdateInfo (
    User user,
    BigDecimal confirmedPaymentTotal
){}
