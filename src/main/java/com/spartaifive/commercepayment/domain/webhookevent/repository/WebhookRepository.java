package com.spartaifive.commercepayment.domain.webhookevent.repository;

import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook,Long> {
    boolean existsByWebhookId(String webhookId);
    Optional<Webhook> findByWebhookId(String webhookId);
}
