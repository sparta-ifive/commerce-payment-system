package com.spartaifive.commercepayment.domain.payment.controller;

import com.spartaifive.commercepayment.common.auth.AuthUtil;
import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.payment.dto.request.PaymentAttemptRequest;
import com.spartaifive.commercepayment.domain.payment.dto.request.RefundRequest;
import com.spartaifive.commercepayment.domain.payment.dto.response.ConfirmPaymentResponse;
import com.spartaifive.commercepayment.domain.payment.dto.response.PaymentAttemptResponse;
import com.spartaifive.commercepayment.domain.payment.dto.response.RefundResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/attempt")
    public ResponseEntity<DataResponse<PaymentAttemptResponse>> createPayment(
            @Valid @RequestBody PaymentAttemptRequest request) {
        Long userId = AuthUtil.getCurrentUserId();
        PaymentAttemptResponse response = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.success(String.valueOf(HttpStatus.CREATED.value()), response));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<DataResponse<ConfirmPaymentResponse>> confirmByPaymentId(
            @PathVariable @NotBlank(message = "paymentId는 필수입니다") String paymentId) {
        Long userId = AuthUtil.getCurrentUserId();
        ConfirmPaymentResponse response = paymentService.confirmByPaymentId(userId, paymentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), response));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<DataResponse<RefundResponse>> refundOrder(
            @PathVariable String paymentId, @Valid @RequestBody RefundRequest request) {
        Long userId = AuthUtil.getCurrentUserId();
        RefundResponse response = paymentService.refundOrder(userId, paymentId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), response));
    }
}
