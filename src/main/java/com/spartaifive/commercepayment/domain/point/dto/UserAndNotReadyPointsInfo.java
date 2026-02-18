package com.spartaifive.commercepayment.domain.point.dto;

import java.math.BigDecimal;

public record UserAndNotReadyPointsInfo(
    Long userId,
    Long membershipRate,
    BigDecimal paymentTotal
)  {}
