package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.common.audit.AuditTxService;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;
import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import com.spartaifive.commercepayment.domain.webhookevent.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.spartaifive.commercepayment.common.exception.ErrorCode.ERR_PORTONE_RESPONSE_NULL;
import static com.spartaifive.commercepayment.common.exception.ErrorCode.ERR_WEBHOOK_PROCESS_FAILED;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final PortOneClient portOneClient;
    private final PaymentService paymentService;
    private final AuditTxService auditTxService;

    @Transactional
    public void handleWebhookEvent(WebhookDto.RequestWebhook webhookDto) {

        String webhookId = webhookDto.getWebhookId();
        String paymentId = webhookDto.getPaymentId();
        LocalDateTime receivedAt = webhookDto.getReceivedAt();

        // 1) webhook-id 멱등 처리
        //    - webhook-id UNIQUE로 이벤트 기록(webhook_event 테이블)
        //    - 이미 처리된 webhook-id면 즉시 200 반환
        if (webhookRepository.existsByWebhookId(webhookId)) {
            log.info("[PORTONE_WEBHOOK] duplicated webhook, id={}", webhookId);
            return;
        }

        Webhook webhook = new Webhook(webhookId, paymentId, receivedAt);
        auditTxService.savedWebhookReceived(webhook);

        // 2) paymentId로 PortOne 결제 조회
        //    - status / amount 확인
        //    - 주문 금액과 비교
        try {
            //포트원과 데이터 확인
            PortOnePaymentResponse portOne = portOneClient.getPayment(paymentId);
            if (portOne == null) {
                throw new ServiceErrorException(ERR_PORTONE_RESPONSE_NULL);
            }

            paymentService.syncFromPortOneWebhook(paymentId, portOne);
            auditTxService.markWebhookProcessed(webhookId);
            log.info(
                    "[PORTONE_WEBHOOK] processed successfully. webhookId={}, paymentId={}",
                    webhookId,
                    paymentId
            );
        } catch (Exception e) {
            auditTxService.markWebhookFailed(webhookId);
            log.error(
                    "[PORTONE_WEBHOOK] processed failed. webhookId={}, paymentId={}",
                    webhookId,
                    paymentId
            );
            throw new ServiceErrorException(ERR_WEBHOOK_PROCESS_FAILED); // 웹훅 재시도 유도
        }
    }
}
