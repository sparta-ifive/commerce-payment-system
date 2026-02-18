package com.spartaifive.commercepayment.domain.webhookevent.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.spartaifive.commercepayment.common.service.TimeService;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "webhook_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Webhook {

    //우리 db에서 관리할 키
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 포트원이랑 통신할때 사용할 키
    @Column(nullable = false, unique = true)
    private String webhookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false)
    private String paymentId;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    private LocalDateTime completedAt;

    public Webhook(String webhookId, String paymentId, LocalDateTime receivedAt) {
        this.webhookId = webhookId;
        this.paymentId = paymentId;
        this.status = EventStatus.RECEIVED;
        this.receivedAt = receivedAt;
    }

    public void processed() {
        this.status = EventStatus.PROCESSED;
        this.completedAt = TimeService.getCurrentTime();
    }

    public void failed() {
        this.status = EventStatus.FAILED;
        this.completedAt = TimeService.getCurrentTime();
    }
}
