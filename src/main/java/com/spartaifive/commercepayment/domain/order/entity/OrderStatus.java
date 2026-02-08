package com.spartaifive.commercepayment.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PAYMENT_PENDING("PAYMENT_PENDING", "주문 대기"),
    COMPLETED("COMPLETED", "주문 완료"),
    REFUNDED("REFUNDED", "환불");

    private final String statusCode;
    private final String statusDescription;

    OrderStatus(String statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }
}
