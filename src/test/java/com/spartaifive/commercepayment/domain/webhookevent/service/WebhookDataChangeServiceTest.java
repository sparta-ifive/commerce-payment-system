package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookDataChangeServiceTest {

    @InjectMocks
    WebhookDataChangeService service;

    @Mock
    ProductRepository productRepository;
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderProductRepository orderProductRepository;

    @Test
    void 주문수량만큼_재고가_차감된다() {
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
        when(orderProduct.getQuantity()).thenReturn(2L);
        when(orderProduct.getProduct()).thenReturn(product);

        when(paymentRepository.findByPortonePaymentId(any()))
                .thenReturn(Optional.of(payment));
        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(orderProductRepository.findAllByOrder_Id(any()))
                .thenReturn(List.of(orderProduct));
        when(productRepository.findById(any()))
                .thenReturn(Optional.of(product));

        // when
        service.changeStock(dto);

        // then
        verify(product).minusStock(2L);
    }
}

