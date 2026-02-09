package com.spartaifive.commercepayment.domain.product.entity;

import lombok.Getter;

// TODO: 시간나면 카테고리 테이블을 따로 만들기
@Getter
public enum ProductCategory {
    ELECTRONICS("ELECTORNICS", "전자기기") ,
    FOOD("FOOD", "음식") ,
    TOY("TOY", "장남감"),
    CLOTHES("CLOTHES", "의류");

    private final String categoryCode;
    private final String categoryDescription;

    ProductCategory(String categoryCode, String categoryDescription) {
        this.categoryCode = categoryCode;
        this.categoryDescription = categoryDescription;
    }
}
