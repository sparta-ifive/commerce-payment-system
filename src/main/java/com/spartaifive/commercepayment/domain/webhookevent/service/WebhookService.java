package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.webhookevent.PortoneWebhookPayload;
import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import com.spartaifive.commercepayment.domain.webhookevent.repository.WebhookRepository;
import io.portone.sdk.server.payment.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final PortOneClient portOneClient;
    private final WebhookSupportService supportService;

    @Transactional
    public void handleWebhookEvent(WebhookDto.RequestWebhook webhookDto) {

        String webhookId = webhookDto.getWebhookId();
        String paymentId = webhookDto.getPaymentId();
        LocalDateTime receivedAt = webhookDto.getReceivedAt();

        //주석 처리 문장들: todo
        // 1) webhook-id 멱등 처리
        //    - webhook-id UNIQUE로 이벤트 기록(webhook_event 테이블)
        //    - 이미 처리된 webhook-id면 즉시 200 반환
        if (webhookRepository.existsByWebhookId(webhookId)) {
            log.info("[PORTONE_WEBHOOK] duplicated webhook, id={}", webhookId);
            return;
        }

        Webhook webhook = new Webhook(webhookId, paymentId, receivedAt);
        Webhook savedWebhook = webhookRepository.save(webhook);

        // 2) paymentId로 PortOne 결제 조회(SSOT)
        //    - status / amount 확인
        //    - 주문 금액과 비교
        // 포트원에서 paymentId로 조회한 결제정보를 담은 dto
        try {
            //포트원과 데이터 확인
            PortOnePaymentResponse paymentResponse = portOneClient.getPayment(paymentId);
            supportService.validate(webhookDto, paymentResponse);
            savedWebhook.processed();
        } catch (Exception e) {
            savedWebhook.failed();
            log.info(
                    "[PORTONE_WEBHOOK] processed failed. webhookId={}, paymentId={}",
                    webhookId,
                    paymentId
            );
            throw e;
        }

    }
}
