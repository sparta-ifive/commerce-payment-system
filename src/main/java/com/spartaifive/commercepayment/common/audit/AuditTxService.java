package com.spartaifive.commercepayment.common.audit;

import com.spartaifive.commercepayment.domain.refund.entity.Refund;
import com.spartaifive.commercepayment.domain.refund.repository.RefundRepository;
import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import com.spartaifive.commercepayment.domain.webhookevent.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditTxService {

    private final RefundRepository refundRepository;
    private final WebhookRepository webhookRepository;

    // ---- Refund ----
    @Transactional
    public void markRefundFailed(Refund refund, String message) {
        refund.fail(message);
        refundRepository.save(refund);
    }

    @Transactional
    public void markRefundCompleted(Refund refund) {
        refund.complete();
        refundRepository.save(refund);
    }

    @Transactional
    public Refund saveRefundRequested(Refund refund) {
        return refundRepository.save(refund);
    }

    // ---- Webhook Event ----
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savedWebhookReceived(Webhook webhook) {
        webhookRepository.save(webhook);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWebhookProcessed(String webhookId) {
        Webhook webhook = webhookRepository.findByWebhookId(webhookId).orElseThrow(
                () -> new IllegalStateException("webhook not found id=" + webhookId)
        );
        webhook.processed();
        webhookRepository.save(webhook);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWebhookFailed(String webhookId) {
        Webhook webhook = webhookRepository.findByWebhookId(webhookId).orElseThrow(
                () -> new IllegalStateException("webhook not found id=" + webhookId)
        );
        webhook.failed();
        webhookRepository.save(webhook);
    }
}
