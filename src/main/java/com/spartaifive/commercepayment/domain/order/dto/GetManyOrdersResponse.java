package com.spartaifive.commercepayment.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetManyOrdersResponse {
    private final Long orderId;
    private final UUID orderNumber;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Long userId;

    public static GetManyOrdersResponse of(Order order) {
        return new GetManyOrdersResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getModifiedAt(),
                order.getUser().getId()
        );
    }
}

