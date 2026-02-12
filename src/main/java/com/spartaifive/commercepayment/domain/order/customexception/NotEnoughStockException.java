package com.spartaifive.commercepayment.domain.order.customexception;

import lombok.Getter;

@Getter
public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(String message) {
        super(message);
    }
}
