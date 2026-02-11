package com.spartaifive.commercepayment.domain.payment.dto;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PaymentOrderEntities {
    private final Payment payment;
    private final Order order;
    private final List<OrderProduct> orderProducts;
    private final List<Product> products;
}
