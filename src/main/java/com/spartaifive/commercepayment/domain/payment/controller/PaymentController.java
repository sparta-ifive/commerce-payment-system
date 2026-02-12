package com.spartaifive.commercepayment.domain.payment.controller;

import com.spartaifive.commercepayment.common.auth.AuthUtil;
import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.service.OrderService;
import com.spartaifive.commercepayment.domain.payment.dto.request.ConfirmPaymentRequest;
import com.spartaifive.commercepayment.domain.payment.dto.request.PaymentAttemptRequest;
import com.spartaifive.commercepayment.domain.payment.dto.request.RefundRequest;
import com.spartaifive.commercepayment.domain.payment.dto.response.ConfirmPaymentResponse;
import com.spartaifive.commercepayment.domain.payment.dto.response.PaymentAttemptResponse;
import com.spartaifive.commercepayment.domain.payment.dto.response.RefundResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/attempt")
    public ResponseEntity<DataResponse<PaymentAttemptResponse>> createPayment(
            @Valid @RequestBody PaymentAttemptRequest request) {
        Long userId = AuthUtil.getCurrentUserId();
        PaymentAttemptResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.success(HttpStatus.CREATED.name(), response));
    }

    @PostMapping("/confirm")
    public ResponseEntity<DataResponse<ConfirmPaymentResponse>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request) {
        Long userId = AuthUtil.getCurrentUserId();
        ConfirmPaymentResponse response = paymentService.confirmPayment(userId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(DataResponse.success(HttpStatus.OK.name(), response));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<DataResponse<ConfirmPaymentResponse>> confirmByPaymentId(
            @PathVariable String paymentId) {
        Long userId = AuthUtil.getCurrentUserId();
        ConfirmPaymentResponse response = paymentService.confirmByPaymentId(userId, paymentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(DataResponse.success(HttpStatus.OK.name(), response));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<DataResponse<RefundResponse>> refundOrder(
            @PathVariable String paymentId, @Valid @RequestBody RefundRequest request) {
        Long userId = AuthUtil.getCurrentUserId();
        RefundResponse response = paymentService.refundOrder(userId, paymentId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success(HttpStatus.OK.name(), response));
    }
}
