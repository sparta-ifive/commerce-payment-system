package com.spartaifive.commercepayment.domain.webhookevent.service;

import com.spartaifive.commercepayment.common.external.portone.PortOneClient;
import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.webhookevent.PortoneWebhookPayload;
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
    public final WebhookRepository webhookRepository;
    public final PortOneClient portOneClient;
    public final OrderRepository orderRepository;

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
            PortOnePaymentResponse paymentResponse = portOneClient.getPayment(paymentId);
            if(!paymentResponse.isPaid()) {
                throw new IllegalStateException("결제 상태가 완료가 아닙니다.");
            }

            //Order order  = orderRepository.findBy
            //if(payment의 상태 확인) throw new IllegalStateException("결제 상태 오류");
            // Order order = orderService.findByPaymentId(payment.id());
            // if (order 의 합계 컬럼과 직접 계산한 값이 다른 경우) {
            //     throw new IllegalStateException("결제 금액 불일치");
            // }

            // 3) 결제/주문 상태 반영(트랜잭션)
            //    - 결제 상태 전이 검증
            //      - 막아야 하는 전이 체크 (예: REFUNDED → PAID : 이미 환불된 결제)
            //    - 재고 차감 후 확정
            //    - 성공 시 결제=결제완료, 주문=주문완료
            //
            // 4) 처리 완료 마킹
            //    - webhook_event 테이블의 처리 완료 시각 업데이트
            savedWebhook.processed();
            log.info(
                    "[PORTONE_WEBHOOK] processed successfully. webhookId={}, paymentId={}",
                    webhookId,
                    paymentId
            );
        } catch (Exception e) {
            // 이 부분 작성하기 대충 작성하였음
            webhook.failed();
            log.info(
                    "[PORTONE_WEBHOOK] processed failed. webhookId={}, paymentId={}",
                    webhookId,
                    paymentId
            );
            throw e;
        }

    }
}
