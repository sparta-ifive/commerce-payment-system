package com.spartaifive.commercepayment.domain.refund.entity;

import lombok.Getter;

@Getter
public enum RefundStatus {
    REQUESTED("REQUESTED", "환불 요청"),
    COMPLETED("COMPLETED", "환불 완료"),
    FAILED("FAILED", "환불 실패");

    private final String statusCode;
    private final String statusDescription;

    RefundStatus(String statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }
}
