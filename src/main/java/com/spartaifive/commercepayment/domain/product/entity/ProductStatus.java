package com.spartaifive.commercepayment.domain.product.entity;

import lombok.Getter;

@Getter
public enum ProductStatus {
    ON_SALE("ON_SALE", "판매중") ,
    OUT_OF_STOCK("OUT_OF_STOCK", "품절") ,
    DISCONTINUED("DISCONTINUED", "단종");

    private final String statusCode;
    private final String statusDescription;

    ProductStatus(String statusCode, String statusDescription) {
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }
}