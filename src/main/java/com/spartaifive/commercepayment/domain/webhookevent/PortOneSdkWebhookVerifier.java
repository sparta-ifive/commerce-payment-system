package com.spartaifive.commercepayment.domain.webhookevent;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PortOne 공식 Server SDK를 이용한 웹훅 시그니처 검증기.
 *
 * <p>기존 {@link PortOneWebhookVerifier}가 HMAC-SHA256을 직접 구현한 반면,
 * 이 클래스는 {@code io.portone:server-sdk}의 {@link WebhookVerifier}에 위임하여
 * Standard Webhooks 스펙에 맞는 검증을 수행한다.</p>
 *
 * <p>SDK가 검증과 역직렬화를 동시에 처리하므로,
 * 검증 성공 시 {@link Webhook} 타입 객체를 바로 반환받을 수 있다.</p>
 */
@Component
@Slf4j
public class PortOneSdkWebhookVerifier {

    @Value("${portone.webhook.secret}")
    private String webhookSecret;

    private WebhookVerifier webhookVerifier;

    @PostConstruct
    void init() {
        this.webhookVerifier = new WebhookVerifier(webhookSecret);
        log.info("[PortOneSdkWebhookVerifier] initialized with SDK WebhookVerifier");
    }

    /**
     * 웹훅 메시지를 검증하고 파싱된 Webhook 객체를 반환한다.
     *
     * @param msgBody      요청 본문 (JSON 문자열 원문 그대로)
     * @param msgId        webhook-id 헤더
     * @param msgSignature webhook-signature 헤더
     * @param msgTimestamp webhook-timestamp 헤더
     * @return 검증 및 역직렬화된 {@link Webhook} 객체
     * @throws WebhookVerificationException 시그니처 검증 실패 시
     */
    public Webhook verify(String msgBody, String msgId, String msgSignature, String msgTimestamp)
            throws WebhookVerificationException {
        return webhookVerifier.verify(msgBody, msgId, msgSignature, msgTimestamp);
    }
}
