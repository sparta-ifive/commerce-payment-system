package com.spartaifive.commercepayment.domain.webhookevent.controller;

import com.spartaifive.commercepayment.domain.webhookevent.PortOneSdkWebhookVerifier;
import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
import com.spartaifive.commercepayment.domain.webhookevent.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransaction;
import io.portone.sdk.server.webhook.WebhookTransactionData;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final PortOneSdkWebhookVerifier verifier;
    private final WebhookService webhookService;

    @PostMapping(value = "/portone-webhook", consumes = "application/json")
    public ResponseEntity<Void> handlePortoneWebhook(

            // 1. 검증용 원문 (SDK verify()는 String을 받으므로 String으로 수신)
            @RequestBody String rawBody,

            // 2. PortOne V2 필수 헤더
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature
    ) {
        log.info("[PORTONE_WEBHOOK] id={} ts={}", webhookId, webhookTimestamp);

        // 3. SDK를 이용한 시그니처 검증 + 역직렬화
        Webhook webhook;
        try {
            webhook = verifier.verify(rawBody, webhookId, webhookSignature, webhookTimestamp);
        } catch (WebhookVerificationException e) {
            log.warn("[PORTONE_WEBHOOK] signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 4. 검증 통과 – 웹훅 타입별 분기 처리
        if (webhook instanceof WebhookTransaction transaction) {
            WebhookTransactionData data = transaction.getData();
            log.info(
                    "[PORTONE_WEBHOOK] VERIFIED Transaction paymentId={} transactionId={} storeId={} timestamp={}",
                    data.getPaymentId(),
                    data.getTransactionId(),
                    data.getStoreId(),
                    transaction.getTimestamp()
            );

            //5. 검증 통과한 웹훅을 dto로 변환하여 서비스에 넘기기
            LocalDateTime receivedAt = LocalDateTime.ofInstant(transaction.getTimestamp(), ZoneId.systemDefault());
            WebhookDto.RequestWebhook webhookDto = new WebhookDto.RequestWebhook(webhookId, data.getPaymentId(),receivedAt);
            webhookService.handleWebhookEvent(webhookDto);

        } else {
            log.info("[PORTONE_WEBHOOK] VERIFIED non-transaction webhook: {}", webhook.getClass().getSimpleName());
        }

        return ResponseEntity.ok().build();
    }
}