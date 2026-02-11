package com.spartaifive.commercepayment.domain.payment.controller;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.service.OrderService;
import com.spartaifive.commercepayment.domain.payment.dto.ConfirmPaymentRequest;
import com.spartaifive.commercepayment.domain.payment.dto.ConfirmPaymentResponse;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentAttemptRequest;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentAttemptResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;
import com.spartaifive.commercepayment.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/attempt")
    public ResponseEntity<DataResponse<PaymentAttemptResponse>> createPayment(
            @AuthenticationPrincipal User user, @Valid @RequestBody PaymentAttemptRequest request) {
        PaymentAttemptResponse response = paymentService.createPayment(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.success(HttpStatus.CREATED.name(), response));
    }

    @PostMapping("/confirm")
    public ResponseEntity<DataResponse<ConfirmPaymentResponse>> confirmPayment(
            @AuthenticationPrincipal User user, @Valid @RequestBody ConfirmPaymentRequest request) {
        ConfirmPaymentResponse response = paymentService.confirmPayment(user.getId(), request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(DataResponse.success(HttpStatus.OK.name(), response));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<DataResponse<ConfirmPaymentResponse>> confirmByPaymentId(
            @AuthenticationPrincipal User user, @PathVariable String paymentId) {
        ConfirmPaymentResponse response = paymentService.confirmByPaymentId(user.getId(), paymentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(DataResponse.success(HttpStatus.OK.name(), response));
    }
}
