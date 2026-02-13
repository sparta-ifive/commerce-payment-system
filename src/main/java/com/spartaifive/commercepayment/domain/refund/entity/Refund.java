package com.spartaifive.commercepayment.domain.refund.entity;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refunds",
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_refunds_payment_id", columnNames = "payment_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refunds_payment"))
    private Payment payment;

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "refundReason", nullable = false, length = 100)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "fail_reason", length = 100)
    private String failReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    public static Refund request(Payment payment, BigDecimal refundAmount, String refundReason) {
        if (payment == null) {
            throw new IllegalArgumentException("결제가 존재하지 않습니다");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("환불 금액은 0 이상이어야 합니다");
        }
        if (refundReason == null || refundReason.isBlank()) {
            throw new IllegalArgumentException("환불 사유는 필수 입니다");
        }

        Refund refund = new Refund();
        refund.payment = payment;
        refund.amount = refundAmount;
        refund.reason = refundReason;
        refund.status = RefundStatus.REQUESTED;

        return refund;
    }

    public void complete() {
        this.status = RefundStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void fail(String failReason) {
        this.status = RefundStatus.FAILED;
        this.failReason = failReason;
        this.processedAt = LocalDateTime.now();
    }

}
