package com.spartaifive.commercepayment.domain.webhookevent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class WebhookDto {

    @Getter
    @AllArgsConstructor
    public static class RequestWebhook{
        @NotNull(message = "webhookId를 확인하지 못했습니다")
        private String webhookId;
        @NotNull(message = "paymentId를 확인하지 못했습니다")
        private String paymentId;
        @NotNull @CreatedDate
        private LocalDateTime receivedAt;
    }
}
