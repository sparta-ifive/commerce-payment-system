package com.spartaifive.commercepayment.common.exception;

import org.springframework.http.HttpStatus;

public class PortOneApiException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public PortOneApiException(String errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public PortOneApiException(String errorCode, String errorMessage, int statusCode) {
        this(errorCode, errorMessage, HttpStatus.valueOf(statusCode));
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
