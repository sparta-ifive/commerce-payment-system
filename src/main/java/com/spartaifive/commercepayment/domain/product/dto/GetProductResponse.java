package com.spartaifive.commercepayment.domain.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetProductResponse {
    private final Long productId;
    private final String name;
    private final BigDecimal price;
    private final Long stock;
    private final ProductStatus status;
    private final ProductCategory category;
    private final String description;

    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public static GetProductResponse of(Product product) {
        return new GetProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            product.getStatus(),
            product.getCategory(),
            product.getDescription(),

            product.getCreatedAt(),
            product.getModifiedAt()
        );
    }
}

