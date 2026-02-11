package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import com.spartaifive.commercepayment.domain.webhookevent.repository.WebhookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebhookService {
    private final WebhookRepository webhookRepository;
    private final PortOneClient portOneClient;
//    private final WebhookValidationService validationService;
//    private final WebhookDataChangeService changeService;

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

        // 2) paymentId로 PortOne 결제 조회
        //    - status / amount 확인
        //    - 주문 금액과 비교
        try {
            //Todo: 결제가 현재 불가능하기 때문에 주석처리 부분(검증하는 부분) 살리면 오류가 남. 추후 확인 필요
            // *:  //* 붙어있는 코드들은 추후 살려야 하는 코드들임
            //포트원과 데이터 확인
//*            PortOnePaymentResponse paymentResponse = portOneClient.getPayment(paymentId);
//*            validationService.validate(webhookDto, paymentResponse);
//*           changeService.changeStock(webhookDto);
//*            validationService.updatePaymentConfirmed();
            savedWebhook.processed();
            log.info(
                    "[PORTONE_WEBHOOK] processed successfully. webhookId={}, paymentId={}",
                    webhookId,
                    paymentId
            );
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
