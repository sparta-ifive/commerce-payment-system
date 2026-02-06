package com.spartaifive.commercepayment.domain.webhookevent.entity;

import lombok.Getter;

@Getter
public enum EventStatus {
    RECEIVED("RECEIVED", "수신됨"),
    PROCESSED("PROCESSED", "처리 완료") ,
    FAILED("FAILED", "실패");

    private final String statusCode;
    private final String statusDescription;

    EventStatus(String statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }
}
