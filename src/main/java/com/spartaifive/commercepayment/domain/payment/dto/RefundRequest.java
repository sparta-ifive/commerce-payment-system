package com.spartaifive.commercepayment.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank(message = "환불 사유는 필수 입니다")
        String refundReason
) {
}
