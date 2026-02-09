package com.spartaifive.commercepayment.domain.webhookevent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final PortOneWebhookVerifier verifier;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/portone-webhook", consumes = "application/json")
    public ResponseEntity<Void> handlePortoneWebhook(

            // 1. 검증용 원문
            @RequestBody byte[] rawBody,

            // 2. PortOne V2 필수 헤더
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature
    ) {
        // (선택) 원문 로그
        log.info(
                "[PORTONE_WEBHOOK] id={} ts={} body={}",
                webhookId,
                webhookTimestamp,
                new String(rawBody, StandardCharsets.UTF_8)
        );

        // 3. 시그니처 검증 (rawBody 기준)
        boolean verified = verifier.verify(
                rawBody,
                webhookId,
                webhookTimestamp,
                webhookSignature
        );

        if (!verified) {
            log.warn("[PORTONE_WEBHOOK] signature verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 4. 검증 통과 후 DTO 변환
        PortoneWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, PortoneWebhookPayload.class);
        } catch (Exception e) {
            log.error("[PORTONE_WEBHOOK] payload parse failed", e);
            return ResponseEntity.badRequest().build();
        }

        // 5. 이후부터는 “신뢰 가능한 데이터”
        log.info(
                "[PORTONE_WEBHOOK] VERIFIED paymentId={} status={}",
                payload.getPaymentId(),
                payload.getStatus()
        );

        // TODO (Webhook 처리 - 실습 구현 포인트)
        //
        // 1) webhook-id 멱등 처리
        //    - webhook-id UNIQUE로 이벤트 기록(webhook_event 테이블)
        //    - 이미 처리된 webhook-id면 즉시 200 반환
        //
        // 2) paymentId로 PortOne 결제 조회(SSOT)
        //    - status / amount 확인
        //    - 주문 금액과 비교
        //
        // 3) 결제/주문 상태 반영(트랜잭션)
        //    - 결제 상태 전이 검증
        //      - 막아야 하는 전이 체크 (예: REFUNDED → PAID : 이미 환불된 결제)
        //    - 재고 차감 후 확정
        //    - 성공 시 결제=결제완료, 주문=주문완료
        //
        // 4) 처리 완료 마킹
        //    - webhook_event 테이블의 처리 완료 시각 업데이트

        return ResponseEntity.ok().build();
    }
}
