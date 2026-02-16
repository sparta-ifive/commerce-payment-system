package com.spartaifive.commercepayment.common.constatns;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Getter
public class Constants {
    // 환불 가능 기간
    @Value("${app.business.refund-period:7d}")
    private Duration refundPeriod;
}
