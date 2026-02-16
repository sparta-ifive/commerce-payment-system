package com.spartaifive.commercepayment.common.exception;

import lombok.Getter;

@Getter
public class ServiceErrorException extends RuntimeException {
    private final ErrorCode errorCode;

    public ServiceErrorException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ServiceErrorException(ErrorCode errorCode, String overrideMessage) {
        super(overrideMessage);
        this.errorCode = errorCode;
    }
}
