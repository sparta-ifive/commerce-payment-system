package com.spartaifive.commercepayment.domain.payment.entity;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_portone_payment_id",
                        columnNames = "portone_payment_id"),
                @UniqueConstraint(name = "uk_payments_merchant_payment_id",
                        columnNames = "merchant_payment_id")
        },
        indexes = {
        @Index(name = "ix_payments_order_id", columnList = "order_id"),
        @Index(name = "ix_payments_user_id", columnList = "user_id"),
        @Index(name = "ix_payments_status", columnList = "status")
        }
)
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // payment Record ID

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_order"))
    private Order order;
    /**
     * PortOne 결제 결과 ID
     * - Attempt 단계에서는 null
     * - Confirm/Webhook에서 세팅
     * - UNIQUE로 멱등성 보장
     */
    @Column(name = "portone_payment_id", length = 100)
    private String portonePaymentId;
    /**
     * 결제 요청 식별자
     * - 주문 / 결제 조회 ID
     */
    @Column(name = "merchant_payment_id", length = 100)
    private String merchantPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;
    /**
     * 서버가 예상하는 결제 금액 (스냅샷)
     */
    @Column(name = "expected_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedAmount;
    /**
     * PortOne에서 조회한 결제 승인 금액
     */
    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_reason")
    private String refundReason;

    public static Payment createAttempt(
            Long userId, Order order, BigDecimal expectedAmount, String merchantPaymentId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId가 존재하지 않습니다");
        }
        if (order == null) {
            throw new IllegalArgumentException("주문이 존재하지 않습니다");
        }
        if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("주문 금액(예상 결제 금액)은 0보다 커야합니다");
        }
        if (merchantPaymentId == null) {
            throw new IllegalArgumentException("merchantPaymentId가 존재하지 않습니다");
        }
        Payment payment = new Payment();
        payment.userId = userId;
        payment.order = order;
        payment.expectedAmount = expectedAmount;
        payment.merchantPaymentId = merchantPaymentId;
        payment.paymentStatus = PaymentStatus.READY;
        payment.attemptedAt = LocalDateTime.now();
        payment.updatedAt = LocalDateTime.now();

        return payment;
    }

    public Payment confirm(String portonePaymentId, BigDecimal actualAmount, LocalDateTime paidAt) {
        validatePaymentStatus(PaymentStatus.PAID);

        if (portonePaymentId == null || portonePaymentId.isBlank()) {
            throw new IllegalArgumentException("portonePaymentId가 존재하지 않습니다");
        }

        if (this.portonePaymentId != null && !Objects.equals(portonePaymentId, this.portonePaymentId)) {
            throw new IllegalStateException("이미 다른 portonePaymentId로 확정/실패 처리된 결제입니다");
        }

        this.portonePaymentId = portonePaymentId;
        this.actualAmount = actualAmount;
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now(); // portone paidAt이 없으면 씌우기
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    public Payment fail(String portonePaymentId) {
        validatePaymentStatus(PaymentStatus.FAILED);

        if (portonePaymentId != null && !portonePaymentId.isBlank()) {
            if (this.portonePaymentId != null && !Objects.equals(this.portonePaymentId, portonePaymentId)) {
                throw new IllegalStateException("이미 다른 portonePaymentId로 확정/실패 처리된 결제입니다");
            }
            this.portonePaymentId = portonePaymentId;
        }

        this.paymentStatus =  PaymentStatus.FAILED;
        this.failedAt =LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        return this;
    }

    public Payment refund(String reason) {
        validatePaymentStatus(PaymentStatus.REFUNDED);

        this.refundReason = reason;
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    private void validatePaymentStatus(PaymentStatus paymentStatus) {
        if (!this.paymentStatus.canTransition(paymentStatus)) {
            throw new IllegalStateException("결제 상태 전이가 불가능합니다");
        }
    }
}
