package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.payment.service.PaymentSupportService;
import com.spartaifive.commercepayment.domain.webhookevent.PortoneWebhookPayload;
import com.spartaifive.commercepayment.domain.webhookevent.entity.WebhookEvent;
import com.spartaifive.commercepayment.domain.webhookevent.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebhookEventService {
    public final WebhookEventRepository webhookEventRepository;
    public final PortOneClient portOneClient;
    private final PaymentSupportService paymentSupportService;

    @Transactional
    public void handleWebhookEvent(String webhookId, PortoneWebhookPayload payload) {

        //주석 처리 문장들: todo
        // 1) webhook-id 멱등 처리
        //    - webhook-id UNIQUE로 이벤트 기록(webhook_event 테이블)
        //    - 이미 처리된 webhook-id면 즉시 200 반환
        if(webhookEventRepository.existsByWebhookId(webhookId)) {
            log.info("[PORTONE_WEBHOOK] duplicated webhook, id={}", webhookId);
            return;
        }

        WebhookEvent webhookEvent = new WebhookEvent(webhookId);
        WebhookEvent savedWebhookEvent = webhookEventRepository.save(webhookEvent);

        // 2) paymentId로 PortOne 결제 조회(SSOT)
        //    - status / amount 확인
        //    - 주문 금액과 비교
        // 포트원에서 paymentId로 조회한 결제정보를 담은 dto
        try {
            PortOnePaymentResponse paymentResponse = portOneClient.getPayment(payload.getPaymentId());
            //if(payment의 상태 확인) throw new IllegalStateException("결제 상태 오류");
            // Order order = orderService.findByPaymentId(payment.id());
            // if (order 의 합계 컬럼과 직접 계산한 값이 다른 경우) {
            //     throw new IllegalStateException("결제 금액 불일치");
            // }

            if (paymentResponse.isPaid()) {
                if (paymentSupportService.shouldDoPayment(payload.getPaymentId(), true)) {
                    paymentSupportService.processPayment(payload.getPaymentId());
                }
            }

        // 3) 결제/주문 상태 반영(트랜잭션)
        //    - 결제 상태 전이 검증
        //      - 막아야 하는 전이 체크 (예: REFUNDED → PAID : 이미 환불된 결제)
        //    - 재고 차감 후 확정
        //    - 성공 시 결제=결제완료, 주문=주문완료
        //
        // 4) 처리 완료 마킹
        //    - webhook_event 테이블의 처리 완료 시각 업데이트
        savedWebhookEvent.processed();
        log.info(
                "[PORTONE_WEBHOOK] processed successfully. webhookId={}, paymentId={}",
                webhookId,
                payload.getPaymentId()
        );
        } catch(Exception e) {
            paymentSupportService.markPaymentAsFail(payload.getPaymentId());
            // 이 부분 작성하기 대충 작성하였음
            webhookEvent.failed();
            log.info(
                    "[PORTONE_WEBHOOK] processed failed. webhookId={}, paymentId={}",
                    webhookId,
                    payload.getPaymentId()
            );
        }
    }
}
