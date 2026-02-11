package com.spartaifive.commercepayment.domain.payment.service;

import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.dto.*;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final PortOneClient portOneClient;

    /**
     * 결제 시도(Attempt) 생성
     * - Order 조회
     * - expectedAmount 결정(= 주문 총액)
     * - Payment.createAttempt로 엔티티 생성
     * - 저장 후 Response DTO 반환
     */
    @Transactional
    public PaymentAttemptResponse createPayment(Long userId, PaymentAttemptRequest request) {
        Order order = orderRepository.findById(request.orderId()).orElseThrow(
                () -> new IllegalArgumentException("주문이 존재하지 않습니다 orderId=" + request.orderId())
        ); // NotFoundException 예외 처리
        BigDecimal expectedAmount = order.getTotalPrice();

        // merchantId 생성
        String merchantPaymentId = "pay_" + UUID.randomUUID();

        Payment payment = Payment.createAttempt(userId, order, expectedAmount, merchantPaymentId);
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentAttemptResponse.from(savedPayment);
    }

    @Transactional
    public ConfirmPaymentResponse confirmByPaymentId(Long userId, String paymentId) {
        // webhook 일때는 userId 검증 skip
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId는 필수 입니다");
        }

        Payment payment = paymentRepository.findLatestReadyByMerchantPaymentId(paymentId).orElseThrow(
                () -> new IllegalArgumentException("결제 대기 상태인 결제가 존재하지 않습니다 paymentId=" + paymentId)
        );

        if (!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("결제 소유자가 아닙니다");
        }

        ConfirmPaymentRequest request = new ConfirmPaymentRequest(paymentId, payment.getOrder().getId());
        return confirmPayment(userId, request);
    }

    /**
     * 결제 확정(Confirm)
     * - Payment 조회 (merchantPaymentId 또는 paymentId 기준)
     * - 이미 확정/실패된 결제인지 상태 검증 => 결제 상태가 READY인지 확인 (멱등 처리)
     * - PortOne 결제 결과 조회 및 검증
     *   · 결제 상태(PAID 여부)
     *   · 결제 금액 검증 (actualAmount == expectedAmount)
     * - PortOne 결제 식별자(portonePaymentId) 저장 (UNIQUE, 멱등성 보장)
     * - 결제 상태를 PAID로 전이
     * - Order 조회
     * - 재고 수량 검증
     * - 재고 차감
     * - 결제 확정 결과 Response DTO 반환
     */
    @Transactional
    public ConfirmPaymentResponse confirmPayment(Long userId, ConfirmPaymentRequest request) {
        // 외부 portonePaymentId, orderId null값 검증
        if (request.portonePaymentId() == null || request.portonePaymentId().isBlank()) {
            throw new IllegalArgumentException("portonePaymentId는 필수 입니다");
        }
        if (request.orderId() == null) {
            throw new IllegalArgumentException("orderId는 필수 입니다");
        }

        // portonePaymentId로 이미 처리된 결제면 그대로 반환 (멱등 처리)
        Optional<Payment> exOpt = paymentRepository.findByPortonePaymentId(request.portonePaymentId());
        if (exOpt.isPresent()) {
            Payment existing = exOpt.get();
            if (!existing.getUserId().equals(userId)) {
                throw new IllegalStateException("결제 소유자가 아닙니다");
            }
            return ConfirmPaymentSuccessResponse.from(existing);
        }

        // orderId로 최신 결제 대기 상태의 결제 조회 (최신 READY)
        Payment payment = paymentRepository.findLatestReadyByOrderId(request.orderId()).orElseThrow(
                () -> new IllegalArgumentException("결제 대기 상태인 결제가 존재하지 않습니다 orderId=" + request.orderId())
        );

        if (!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("결제 소유자가 아닙니다");
        }

        // PortOne 결제 조회
        PortOnePaymentResponse portOneResponse = portOneClient.getPayment(request.portonePaymentId());

        // 결제 상태 검증
        if (!portOneResponse.isPaid()) { // 결제 확정이 아니면 실패
            payment.fail(request.portonePaymentId());
            Payment failedPayment = paymentRepository.save(payment);
            String failReason = "PortOne 결제가 확정 상태가 아닙니다 status =\" + portOneResponse.status()";
            return ConfirmPaymentFailResponse.from(failedPayment, failReason);
//            throw new IllegalStateException("PortOne 결제가 확정 상태가 아닙니다 status =" + portOneResponse.status());
        }

        // 결제 금액 검증
        BigDecimal actualAmount = portOneResponse.amount() != null ? portOneResponse.amount().total() : null;
        if (actualAmount == null) {
            payment.fail(request.portonePaymentId());
            Payment failedPayment = paymentRepository.save(payment);
            throw new IllegalStateException("PortOne 결제 금액을 확인할 수 없습니다");
        }

        // expectAmount != actualAmount 비교 검증
        if (payment.getExpectedAmount().compareTo(actualAmount) != 0) {
            payment.fail(request.portonePaymentId());
            Payment failedPayment = paymentRepository.save(payment);
            throw new IllegalStateException(
                    "결제 금액이 일치하지 않습니다 expected=" + payment.getExpectedAmount()
                            + ", actual=" + actualAmount);
        }

        // 재고 검증 및 차감
        Order order = payment.getOrder();
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder_Id(order.getId());

        // 전체 주문 상품 재고 동시에 확인 검증
        for (OrderProduct op :  orderProducts) {
            Long qty = op.getQuantity();
            Long stock = op.getProduct().getStock();
            if (qty > stock) {
                payment.fail(request.portonePaymentId());
                Payment failedPayment = paymentRepository.save(payment);
                throw new IllegalStateException(
                        "재고 부족: productId=" + op.getProduct().getId()
                                + ", quantity=" + qty + ", stock=" + stock
                );
            }
        }

        // 주문 상품 동시 차감
        for (OrderProduct op :  orderProducts) {
            op.getProduct().decreaseStock(op.getQuantity());
        }

        // 결제 확정 및 상태 전이
        LocalDateTime paidAt = parsePortOneTime(portOneResponse.paidAt());
        payment.confirm(request.portonePaymentId(), actualAmount, paidAt);
        order.setStatusToCompleted();

        Payment saved = paymentRepository.save(payment);
        return ConfirmPaymentSuccessResponse.from(saved);
    }

//    @Transactional

    // LocalDateTime.parse 불가로 임시 구현
    private LocalDateTime parsePortOneTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignored2) {
                return null;
            }
        }
    }
}
