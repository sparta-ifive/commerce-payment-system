package com.spartaifive.commercepayment.domain.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentConfirmFailException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public PaymentConfirmFailException(
            String errorCode,
            Throwable cause,
            HttpStatus httpStatus
    ) {
        super(cause.getMessage(), cause);
        this.errorCode = errorCode;
        this.errorMessage = cause.getMessage();
        this.httpStatus = httpStatus;
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
