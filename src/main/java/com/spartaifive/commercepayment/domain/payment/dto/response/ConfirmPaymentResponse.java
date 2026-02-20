package com.spartaifive.commercepayment.domain.payment.dto.response;

public sealed interface ConfirmPaymentResponse
        permits ConfirmPaymentSuccessResponse, ConfirmPaymentFailResponse {
}
