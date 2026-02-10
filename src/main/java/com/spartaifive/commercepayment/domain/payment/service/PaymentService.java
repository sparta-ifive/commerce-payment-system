package com.spartaifive.commercepayment.domain.payment.service;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentAttemptRequest;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentAttemptResponse;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 결제 시도(Attempt) 생성
     * - Order 조회
     * - expectedAmount 결정(= 주문 총액)
     * - Payment.createAttempt로 엔티티 생성
     * - 저장 후 Response DTO 반환
     */
    @Transactional
    public PaymentAttemptResponse createAttempt(PaymentAttemptRequest request) {
        Order order = orderRepository.findById(request.orderId()).orElseThrow(
                () -> new IllegalArgumentException("주문이 존재하지 않습니다 orderId=" + request.orderId())
        ); // NotFoundException 예외 처리
        BigDecimal expectedAmount = order.getTotalPrice();

        Payment payment = Payment.createAttempt(order, expectedAmount);
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentAttemptResponse.from(savedPayment);
    }

}
