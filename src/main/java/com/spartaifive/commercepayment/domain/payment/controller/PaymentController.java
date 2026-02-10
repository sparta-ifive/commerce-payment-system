package com.spartaifive.commercepayment.domain.payment.controller;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.service.OrderService;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentAttemptRequest;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentAttemptResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<DataResponse<PaymentAttemptResponse>> createAttempt(
            @Valid @RequestBody PaymentAttemptRequest request) {
        PaymentAttemptResponse response = paymentService.createAttempt(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.success(HttpStatus.CREATED.name(), response));
    }
}
