package com.spartaifive.commercepayment.domain.payment.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.dto.*;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.payment.exception.PaymentConfirmFailException;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentSupportService paymentSupportService;

    @Transactional
    public AttemptPaymentResponse attemptPayment(AttemptPaymentRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
            .orElseThrow(()->new RuntimeException(String.format("%s 아이디의 주문을 찾지 못하였습니다", req.getOrderId())));

        String paymentId; 
        // paymentId 생성
        {
            // TODO: user id 도 넣기
            paymentId =
                "ORDER_PAYMENT-" +
                req.getOrderId() + "-" +
                Instant.now().toEpochMilli();
        }

        // TODO: 일단은 주문의 총 금액을 믿기
        //
        // 저희가 주문을 할때 결제 상품의 재고가 떨어졌을 수도 있고 무수히 많은
        // 잘못된 일이 있을 수 있지만... 그건 나중에 해결

        Payment payment = new Payment(order, paymentId, order.getTotalPrice());

        payment = paymentRepository.save(payment);

        return AttemptPaymentResponse.of(payment);
    }

    // 나중에 글로벌
    @Transactional(noRollbackFor = PaymentConfirmFailException.class)
    public ConfirmPaymentResponse confirmPayment(ConfirmPaymentRequest req) {
        try {
            if (paymentSupportService.shouldDoPayment(req.getPortOnePaymentId(), false)) {
                paymentSupportService.processPayment(req.getPortOnePaymentId());
            }
            return new ConfirmPaymentResponse(
                    req.getPaymentId(),
                    req.getPortOnePaymentId(),
                    req.getOrderId());
        } catch(Exception e) {
            paymentSupportService.markPaymentAsFail(req.getPortOnePaymentId());
            throw new PaymentConfirmFailException("PAYMENT_FAIL", e, HttpStatus.INTERNAL_SERVER_ERROR);
            // TODO: 환불 요청 하기
        }
    }
}
