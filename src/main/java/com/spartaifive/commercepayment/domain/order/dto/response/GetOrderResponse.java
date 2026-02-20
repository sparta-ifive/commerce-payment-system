package com.spartaifive.commercepayment.domain.order.dto.response;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GetOrderResponse {
    private final List<ResponseProduct> orderProducts;
    private final ResponseOrder order;

    @Getter
    @RequiredArgsConstructor
    public static class ResponseProduct {
        private final Long productId;
        private final String name;
        private final BigDecimal price;
        private final Long quantity;

        public static ResponseProduct of(OrderProduct op) {
            return new ResponseProduct(
                    // NOTE: ID를 가져오는 건 N+1문제가 발생하지 않습니다.
                    op.getProduct().getId(),
                    op.getProductName(),
                    op.getProductPrice(),
                    op.getQuantity()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ResponseOrder {
        private final Long orderId;
        private final UUID orderNumber;
        private final BigDecimal totalPrice;
        private final OrderStatus orderStatus;
        private final LocalDateTime createAt;
        private final LocalDateTime modifiedAt;
        private final Long userId;

        public static ResponseOrder of(Order order) {
            return new ResponseOrder(
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

    public static GetOrderResponse fromOrderAndOrderProducts(
            Order order,
            List<OrderProduct> orderProducts
    ) {
        List<ResponseProduct> responseProducts = new ArrayList<>();

        for (final OrderProduct op : orderProducts) {
            responseProducts.add(ResponseProduct.of(op));
        }

        ResponseOrder responseOrder = ResponseOrder.of(order);

        return new GetOrderResponse(
            responseProducts,
            responseOrder
        );
    }
}

