package com.spartaifive.commercepayment.domain.webhookevent.repository;

import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook,Long> {
    boolean existsByWebhookId(String webhookId);
}
