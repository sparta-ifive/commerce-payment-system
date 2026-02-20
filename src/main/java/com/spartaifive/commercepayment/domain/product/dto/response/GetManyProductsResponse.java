package com.spartaifive.commercepayment.domain.product.dto.response;

import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class GetManyProductsResponse {
    private final Long productId;
    private final String name;
    private final BigDecimal price;
    private final Long stock;
    private final ProductStatus status;
    private final ProductCategory category;

    public static GetManyProductsResponse of(Product product) {
        return new GetManyProductsResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            product.getStatus(),
            product.getCategory()
        );
    }
}

