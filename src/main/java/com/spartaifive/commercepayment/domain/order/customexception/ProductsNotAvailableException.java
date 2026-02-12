package com.spartaifive.commercepayment.domain.order.customexception;

import java.util.List;

import lombok.Getter;

@Getter
public class ProductsNotAvailableException extends RuntimeException {
    private final List<Long> unAvailableProductIds;

    public ProductsNotAvailableException(
            String message,
            List<Long> unAvailableProductIds
    ) {
        super(message);
        this.unAvailableProductIds = unAvailableProductIds;
    }
}
