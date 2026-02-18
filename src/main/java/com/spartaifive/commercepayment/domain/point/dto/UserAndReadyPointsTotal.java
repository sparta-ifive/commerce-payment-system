package com.spartaifive.commercepayment.domain.point.dto;

import java.math.BigDecimal;

public record UserAndReadyPointsTotal(
    Long userId,
    BigDecimal amount
)  {}
