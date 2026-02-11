package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookValidationServiceTest {

    @InjectMocks
    WebhookValidationService service;

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderProductRepository orderProductRepository;

    @Test
    void 결제금액과_상태가_모두_정상이면_통과() {
        // given
        WebhookDto.RequestWebhook dto =
                new WebhookDto.RequestWebhook("wh1", "pay1", LocalDateTime.now());

        Product product = new Product("이름", new BigDecimal("1500"), 3L, ProductStatus.ON_SALE, ProductCategory.CLOTHES, "설명");
        Order order = new Order(new BigDecimal("1500"), 1L);
        order.setStatusToCompleted();
        OrderProduct orderProduct = new OrderProduct(order, product, 1L);
        Payment payment = Payment.createAttempt(1L, order, new BigDecimal("1500"), "merchantId");
        payment.confirm(
                "pay1",                          // portonePaymentId
                BigDecimal.valueOf(1500),        // actualAmount
                LocalDateTime.now()              // paidAt
        );

        PortOnePaymentResponse.Amount amount =
                new PortOnePaymentResponse.Amount(
                        BigDecimal.valueOf(1500), // total
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(1500),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );
        PortOnePaymentResponse portone =
                new PortOnePaymentResponse(
                        "PAID",
                        "pay_1",
                        "tx_1",
                        "merchant_1",
                        "store_1",
                        null,   // method
                        null,   // channel
                        "v1",
                        null,
                        null,
                        null,
                        "orderName",
                        amount,
                        "KRW",
                        null,   // customer
                        "2026-01-01T00:00:00",
                        null
                );


        when(paymentRepository.findByPortonePaymentId("pay1"))
                .thenReturn(Optional.of(payment));
        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(orderProductRepository.findAllByOrder_Id(any()))
                .thenReturn(List.of(orderProduct));

        // then
        assertDoesNotThrow(() ->
                service.validate(dto, portone)
        );
    }


}