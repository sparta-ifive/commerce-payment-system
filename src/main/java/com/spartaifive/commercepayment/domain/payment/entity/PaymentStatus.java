package com.spartaifive.commercepayment.domain.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY("READY", "결제 대기"),
    PAID("PAID", "결제 완료"),
    FAILED("FAILED", "결제 실패"),
    REFUNDED("REFUNDED", "환불 완료");

    private final String statusCode;
    private final String statusDescription;

    PaymentStatus(String statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }

    public boolean canTransition(PaymentStatus target) {
        if (target == null) {
            return false;
        }

        return switch (this) {
            case READY -> target == PAID || target == FAILED;
            case PAID ->  target == REFUNDED;
            case FAILED, REFUNDED -> false;
        };
    }
}