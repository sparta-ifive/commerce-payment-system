package com.spartaifive.commercepayment.domain.payment.controller;

import com.spartaifive.commercepayment.domain.payment.dto.AttemptPaymentRequest;
import com.spartaifive.commercepayment.domain.payment.dto.AttemptPaymentResponse;
import com.spartaifive.commercepayment.domain.payment.dto.ConfirmPaymentRequest;
import com.spartaifive.commercepayment.domain.payment.dto.ConfirmPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/api/payments/attempt")
    public ResponseEntity<DataResponse<AttemptPaymentResponse>> attemptPayment(
            @RequestBody AttemptPaymentRequest req
    ) {
        AttemptPaymentResponse res = paymentService.attemptPayment(req);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataResponse.success("SUCCESS", res));
    }

    @PostMapping("/api/payments/confirm")
    public ResponseEntity<DataResponse<ConfirmPaymentResponse>> confirmPayment(
            @RequestBody ConfirmPaymentRequest req
    ) {
        ConfirmPaymentResponse res = paymentService.confirmPayment(req);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataResponse.success("SUCCESS", res));
    }
}
