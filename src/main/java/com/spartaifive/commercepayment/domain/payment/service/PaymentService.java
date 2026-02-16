package com.spartaifive.commercepayment.domain.payment.service;

import com.spartaifive.commercepayment.common.audit.AuditTxService;
import com.spartaifive.commercepayment.common.constatns.Constants;
import com.spartaifive.commercepayment.common.external.portone.PortOneCancelRequest;
import com.spartaifive.commercepayment.common.external.portone.PortOneCancelResponse;
import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.dto.request.ConfirmPaymentRequest;
import com.spartaifive.commercepayment.domain.payment.dto.request.PaymentAttemptRequest;
import com.spartaifive.commercepayment.domain.payment.dto.request.RefundRequest;
import com.spartaifive.commercepayment.domain.payment.dto.response.*;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import com.spartaifive.commercepayment.domain.point.service.PointService;
import com.spartaifive.commercepayment.domain.refund.entity.Refund;
import com.spartaifive.commercepayment.domain.refund.entity.RefundStatus;
import com.spartaifive.commercepayment.domain.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final PortOneClient portOneClient;
    private final RefundRepository refundRepository;
    private final AuditTxService auditTxService;
    private final PointService pointService;
    private final Constants constants;

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

        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("환불된 주문은 재결제 할 수 없습니다");
        }

        BigDecimal expectedAmount = order.getTotalPrice();

        // 1000원 이하의 결제는 금지
        {
            BigDecimal tmp = expectedAmount;
            if (request.pointsToUse() != null) {
                tmp = expectedAmount.subtract(request.pointsToUse());
            }
            if (tmp.compareTo(BigDecimal.valueOf(1000)) < 0) {
                throw new IllegalArgumentException(
                        String.format("포인트를 제외, %s원을 결제할려고 하십니다. 1000원 이하의 결제는 불가능 합니다", tmp)
                );
            }
        }

        if (request.pointsToUse() != null) {
            expectedAmount = pointService.validatedAndSubtractPointFromOrderTotalPrice(
                    userId, expectedAmount, request.pointsToUse());
        }

        // merchantId 생성
        String merchantPaymentId = "pay_" + UUID.randomUUID();

        Payment payment = Payment.createAttempt(userId, order, expectedAmount, request.pointsToUse(), merchantPaymentId);
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentAttemptResponse.from(savedPayment);
    }

    @Transactional
    public ConfirmPaymentResponse confirmByPaymentId(Long userId, String paymentId) {
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
        // merchantPaymentId, orderId null값 검증
        if (userId == null)  {
            throw new IllegalArgumentException("userId가 존재하지 않습니다");
        }
        if (request == null) {
            throw new IllegalArgumentException("request가 존재하지 않습니다");
        }
        if (request.merchantPaymentId() == null || request.merchantPaymentId().isBlank()) {
            throw new IllegalArgumentException("merchantPaymentId는 필수 입니다");
        }

        // merchantPaymentId로 결제 조회
        Payment payment = paymentRepository.findByMerchantPaymentId(request.merchantPaymentId()).orElseThrow(
                () -> new IllegalArgumentException("결제 대기 상태인 결제가 존재하지 않습니다 orderId=" + request.orderId())
        );

        // orderId 매칭 검증
        if (!payment.getOrder().getId().equals(request.orderId())) {
            throw new IllegalStateException("orderId가 결제와 일치하지 않습니다");
        }

        // 결제 소유자 검증
        if (!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("결제 소유자가 아닙니다");
        }

        // 이미 PAID면 그대로 반환 (멱등)
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return ConfirmPaymentSuccessResponse.from(payment);
        }

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new IllegalStateException("결제 확정이 불가능한 상태입니다 status=" + payment.getPaymentStatus());
        }

        // PortOne 결제 조회
        PortOnePaymentResponse portOne = portOneClient.getPayment(request.merchantPaymentId());
        if (portOne == null) {
            throw new IllegalStateException("PortOne 응답이 null 입니다 merchantPaymentId=" + request.merchantPaymentId());
        }

        // 결제 상태 검증
        if (!portOne.isPaid()) { // 결제 확정이 아니면 실패
            payment.fail(portOne.transactionId());
            Payment failedPayment = paymentRepository.save(payment);
            String failReason = "PortOne 결제가 확정 상태가 아닙니다 status =" + portOne.status();
            return ConfirmPaymentFailResponse.from(failedPayment, failReason);
        }

        // 결제 금액 검증
        BigDecimal actualAmount = requireActualAmount(portOne);
        validatePaymentAmount(payment, actualAmount, portOne.transactionId());

        // 재고 검증 및 차감
        Order order = payment.getOrder();
        List<OrderProduct> orderProducts = getOrderProducts(order);
        validateAndDecreaseStock(orderProducts, payment, portOne.transactionId());


        // 결제 확정 및 상태 전이
        return applyPaidTransition(payment, order, portOne, actualAmount);
    }

    /// 결제 조회
    @Transactional(readOnly = true)
    public PaymentDetailResponse getLatestPaymentByOrderId(Long userId, Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId는 필수입니다");
        }

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new IllegalArgumentException("주문이 존재하지 않습니다 orderId=" + orderId)
        );

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("주문 소유자가 아닙니다");
        }

        Payment payment = paymentRepository.findLatestByOrderId(orderId).orElseThrow(
                () -> new IllegalArgumentException("해당 주문의 결제가 존재하지 않습니다 orderId=" + orderId)
        );

        if (!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("결제 소유자가 아닙니다");
        }

        return PaymentDetailResponse.from(payment);
    }

    /// 결제 취소 (환불)
    @Transactional
    public RefundResponse refundOrder(Long userId, String merchantPaymentId, RefundRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("userId가 존재하지 않습니다");
        }
        if (merchantPaymentId == null) {
            throw new IllegalArgumentException("paymentId는 필수 입니다");
        }
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("환불 사유는 필수 입니다");
        }

        // merchantId로 결제 조회
        Payment payment = paymentRepository.findByMerchantPaymentId(merchantPaymentId).orElseThrow(
                () -> new IllegalStateException("환불 가능한 결제가 없습니다 merchantPaymentId=" + merchantPaymentId)
        );

        // 결제 소유자 검증
        if (!payment.getUserId().equals(userId)) {
            throw new IllegalStateException("결제 소유자가 아닙니다");
        }

        // 주문 가져오기
        Order order = payment.getOrder();

        // 환불 테이블 기준 멱등 처리
        Optional<Refund> exOpt = refundRepository.findByPayment(payment);
        if (exOpt.isPresent()) {
            Refund existing = exOpt.get();
            if (existing.getStatus() == RefundStatus.COMPLETED) {
                if (payment.getPaymentStatus() != PaymentStatus.REFUNDED) {
                    payment.refund(existing.getReason());
                    paymentRepository.save(payment);
                }
                if (order.getStatus() != OrderStatus.REFUNDED) {
                    order.setStatusToRefund();
                    orderRepository.save(order);
                }
                return RefundResponse.from(payment, order);
            }
            throw new IllegalStateException("이미 환불 이력이 존재합니다 status=" + existing.getStatus());
        }

        // 주문 상태가 PAID 인지 확인
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("환불 가능한 주문 상태가 아닙니다 status=" + order.getStatus());
        }

        // 결제 상태가 PAID 인지 확인
        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("환불 가능한 결제 상태가 아닙니다 status=" + payment.getPaymentStatus());
        }

        // 결제가 환불이 가능한 시기를 놓쳤는지 확인
        {
            LocalDateTime canRefundBefore = payment.getPaidAt().plus(constants.getRefundPeriod());

            if (!LocalDateTime.now().isBefore(canRefundBefore)) {
                // TODO: 환불 가능 기간을 고객이 읽기 쉽게 만들어 돌려주기
                throw new IllegalStateException("환불 가능한 시기를 지나 환불이 불가능 합니다");
            }
        }

        // 전액 환불 금액 스냅샷
        BigDecimal refundAmount = payment.getActualAmount() != null ? payment.getActualAmount() : payment.getExpectedAmount();
        if (refundAmount == null) {
            throw new IllegalStateException("환불 금액을 확인할 수 없습니다 merchantPaymentId=" + payment.getId());
        }

        // Refund 레코드 생성/갱신 REQUESTED
        Refund refund = Refund.request(payment, refundAmount, request.reason());
        refund = auditTxService.saveRefundRequested(refund);

        // PortOne Cancel 호출해서 전액 환불 (fullCancel)
        PortOneCancelResponse cancelResponse;
        try {
            PortOneCancelRequest cancelRequest = PortOneCancelRequest.fullCancel(request.reason());
            cancelResponse = portOneClient.cancelPayment(payment.getMerchantPaymentId(), cancelRequest);
        } catch (Exception e) {
            auditTxService.markRefundFailed(refund, "PortOne 환불 API 호출 실패: " + e.getMessage());
            throw new IllegalStateException("PortOne 환불 API 호출 실패: " +
                    "merchantPaymentId=" + merchantPaymentId, e);
        }

        // portOne 응답 검증 (취소 완료 확인)
        requireCancelSucceeded(cancelResponse, payment.getMerchantPaymentId());

        // 전액 환불 금액 검증
        requireFullRefundAmount(cancelResponse.cancelledTotalAmount(), refundAmount, payment.getMerchantPaymentId());

        // 재고 원복 및 상태 전이
        applyRefundCompletedTransition(payment, order, refund, request.reason());
        return RefundResponse.from(payment, order);
    }


    // webhook
    @Transactional
    public void syncFromPortOneWebhook(String merchantPaymentId, PortOnePaymentResponse portOne) {
        if (merchantPaymentId == null || merchantPaymentId.isBlank()) {
            throw new IllegalArgumentException("merchantPaymentId는 필수 입니다");
        }
        if (portOne == null) {
            throw new IllegalArgumentException("portOne가 존재하지 않습니다");
        }

        // 웹훅 <-> confirm/환불 레이스 컨디션 (동시성 잠금)
        Payment payment = paymentRepository.findByMerchantPaymentId(merchantPaymentId).orElseGet(
                () -> paymentRepository.findByPortonePaymentId(
                        portOne.transactionId()).orElseThrow(
                () -> new IllegalStateException("결제가 존재하지 않습니다 merchantPaymentId=" + merchantPaymentId +
                        ", transactionId=" + portOne.transactionId())
        ));

        Order order = payment.getOrder();

        // 포트원 확정 결제가 서버에서 결제 대기 상태면 확정
        if (portOne.isPaid()) {
            if (payment.getPaymentStatus() == PaymentStatus.PAID)
                return;
            BigDecimal actualAmount = requireActualAmount(portOne);
            validatePaymentAmount(payment, actualAmount, portOne.transactionId());

            List<OrderProduct> orderProducts = getOrderProducts(order);
            validateAndDecreaseStock(orderProducts, payment, portOne.transactionId());

            applyPaidTransition(payment, order, portOne, actualAmount);
            return;
        }

        // 포트원 환불 실패가 서버에서 환불 상태면 리턴
        if (portOne.isCancelled()) {
            BigDecimal cancelled = portOne.amount() != null ? portOne.amount().cancelled() : null;
            BigDecimal total = portOne.amount() != null ? portOne.amount().total() : null;

            // 전액 취소만 내부 REFUNDED 처리
            if (cancelled == null || total == null || cancelled.compareTo(total) != 0) {
                log.warn("부분취소 감지: paymentId={}, cancelled={}, total={}", merchantPaymentId, cancelled, total);
                return;
            }

            if (payment.getPaymentStatus() == PaymentStatus.REFUNDED)
                return;

            Optional<Refund> refundOpt = refundRepository.findByPayment(payment);

            Refund refund;

            if (refundOpt.isPresent()) {
                refund = refundOpt.get();
            } else {
                BigDecimal refundAmount =
                        payment.getActualAmount() != null
                                ? payment.getActualAmount()
                                : payment.getExpectedAmount();
                if (refundAmount == null) {
                    throw new IllegalStateException("환불 금액 스냅샷이 없습니다 merchantPaymentId=" + payment.getMerchantPaymentId());
                }
                refund = Refund.request(payment, refundAmount, "PortOne Webhook cancelled");
                refund = auditTxService.saveRefundRequested(refund);
            }

            applyRefundCompletedTransition(payment, order, refund, refund.getReason());
        }
    }

    /// payment helper method ///
    // 실 결제 금액 조회
    private BigDecimal requireActualAmount(PortOnePaymentResponse portOne) {
        BigDecimal actual = portOne.amount() != null ? portOne.amount().total() : null;
        if (actual == null) {
            throw new IllegalStateException("PortOne 결제 금액을 확인할 수 없습니다");
        }
        return actual;
    }

    // 결제 금액 검증
    private void validatePaymentAmount(Payment payment, BigDecimal actualAmount, String portonePaymentId) {
        BigDecimal expected = payment.getExpectedAmount();
        if (expected == null || actualAmount == null) {
            throw new IllegalStateException("결제 금액 정보가 없습니다 paymentId=" + payment.getId());
        }
        if (expected.compareTo(actualAmount) != 0) {
            payment.fail(portonePaymentId);
            paymentRepository.save(payment);
            throw new IllegalStateException(
                    "결제 금액이 일치하지 않습니다 expected=" + payment.getExpectedAmount()
                            + ", actual=" + actualAmount);
        }
    }

    // 주문 조회
    private List<OrderProduct> getOrderProducts(Order order) {
        return orderProductRepository.findAllByOrder_Id(order.getId());
    }

    // 재고 검증 및 차감
    private void validateAndDecreaseStock(List<OrderProduct> orderProducts, Payment payment, String portonePaymentId) {
        // 전체 재고 동시 검증
        for (OrderProduct op : orderProducts) {
            Long qty = op.getQuantity();
            Long stock = op.getProduct().getStock();
            if (qty > stock) {
                payment.fail(portonePaymentId);
                paymentRepository.save(payment);
                throw new IllegalStateException(
                        "재고 부족: productId=" + op.getProduct().getId()
                                + ", quantity=" + qty + ", stock=" + stock
                );
            }
        }

        // 동시 차감
        for (OrderProduct op : orderProducts) {
            op.getProduct().decreaseStock(op.getQuantity());
        }
    }

    // 결제 확정 및 상태 전이
    private ConfirmPaymentResponse applyPaidTransition(
            Payment payment, Order order, PortOnePaymentResponse portOne, BigDecimal actualAmount) {

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return ConfirmPaymentSuccessResponse.from(payment);
        }

        LocalDateTime paidAt = parsePortOneTime(portOne.paidAt());

        payment.confirm(portOne.transactionId(), actualAmount, paidAt);
        order.setStatusToCompleted();

        Payment saved = paymentRepository.save(payment);
        orderRepository.save(order);

        // point 사용
        if (payment.getPointToSpend() != null) {
            pointService.spendPoint(
                    payment.getId(),
                    order.getId(),
                    payment.getUserId()
            );
        }

        // point 적립
        pointService.createPointAfterPaymentConfirm(
                payment.getId(),
                order.getId(),
                payment.getUserId()
        );

        return ConfirmPaymentSuccessResponse.from(saved);
    }

    // 재고 원복
    private void restoreStock(Order order) {
        List<OrderProduct> orderProducts = getOrderProducts(order);
        orderProducts.forEach(op -> op.getProduct().increaseStock(op.getQuantity()));
    }

    // 상태 전이 및 환불 기록 (스냅샷)
    private void applyRefundCompletedTransition(
            Payment payment, Order order, Refund refund, String reason) {
        // 주문 상태 체크 (재고 2번 차감 방지)
        PaymentStatus before = payment.getPaymentStatus();
        boolean alreadyRefunded = (before == PaymentStatus.REFUNDED);
        boolean stockDecreased = (before == PaymentStatus.PAID);

        // 결제/주문 상태 전이
        payment.refund(reason);
        paymentRepository.save(payment);

        order.setStatusToRefund();
        orderRepository.save(order);

        // 재고 원복
        if (!alreadyRefunded && stockDecreased) {
            restoreStock(order);
        }

        // 환불 완료
        if (refund.getStatus() != RefundStatus.COMPLETED) {
            auditTxService.markRefundCompleted(refund);
        }

        // 포인트 반환
        pointService.voidPoints(
                payment.getId(), order.getId(), payment.getUserId());
    }

    // portOne 응답 검증 (취소 완료 확인)
    private void requireCancelSucceeded(PortOneCancelResponse response, String merchantPaymentId) {
        if (response == null || !response.isSucceeded()) {
            String status = (response == null || response.cancellation() == null)
                    ? null : response.cancellation().status();
            throw new IllegalStateException("PortOne 환불 실패 merchantPaymentId="
                    + merchantPaymentId + ", status=" + status);
        }
    }

    // 전액 환불 금액 검증
    private void requireFullRefundAmount(BigDecimal cancelled, BigDecimal expected, String merchantPaymentId) {
        if (cancelled == null || expected == null || cancelled.compareTo(expected) != 0) {
            throw new IllegalStateException(
                    "PortOne 환불 금액 불일치 merchantPaymentId=" + merchantPaymentId
                            + ", cancelled=" + cancelled + ", expected=" + expected
            );
        }
    }

    private LocalDateTime parsePortOneTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            ZonedDateTime zoned = OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault());
            return zoned.toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ignored2) {
                return null;
            }
        }
    }
}
