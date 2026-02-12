package com.spartaifive.commercepayment.domain.order.customexception;

public class InvalidOrderPriceException extends RuntimeException {
    public InvalidOrderPriceException(String message) {
        super(message);
    }
}
