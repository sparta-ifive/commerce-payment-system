package com.spartaifive.commercepayment.domain.payment.entity;

import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.spartaifive.commercepayment.common.exception.ErrorCode.*;

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
    @Column(name = "actual_amount", nullable = true, precision = 15, scale = 2)
    private BigDecimal actualAmount;
    /**
     * 사용자가 쓸 포인트 양 (스냅샷)
     */
    @Column(name = "point_to_spend", nullable = true, precision = 15, scale = 2)
    private BigDecimal pointToSpend;

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
            Long userId, Order order, BigDecimal expectedAmount, BigDecimal pointToSpend, String merchantPaymentId) {
        if (userId == null) {
            throw new ServiceErrorException(ERR_NOT_VALID_VALUE, "userId는 필수입니다");
        }
        if (order == null) {
            throw new ServiceErrorException(ERR_ORDER_NOT_FOUND);
        }
        if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceErrorException(ERR_NOT_VALID_VALUE, "주문 금액(예상 결제 금액)은 0보다 커야합니다");
        }
        // 네, null 일 수도 있습니다.
        if (pointToSpend != null && pointToSpend.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceErrorException(ERR_NOT_VALID_VALUE, "포인트 사용금액은은 0이상 이어야 합니다");
        }
        if (merchantPaymentId == null) {
            throw new ServiceErrorException(ERR_NOT_VALID_VALUE, "merchantPaymentId가 존재하지 않습니다");
        }
        Payment payment = new Payment();
        payment.userId = userId;
        payment.order = order;
        payment.expectedAmount = expectedAmount;
        payment.pointToSpend = pointToSpend;
        payment.merchantPaymentId = merchantPaymentId;
        payment.paymentStatus = PaymentStatus.READY;
        payment.attemptedAt = LocalDateTime.now();
        payment.updatedAt = LocalDateTime.now();

        return payment;
    }

    public Payment confirm(String portonePaymentId, BigDecimal actualAmount, LocalDateTime paidAt) {
        validatePaymentStatus(PaymentStatus.PAID);

        if (portonePaymentId == null || portonePaymentId.isBlank()) {
            throw new ServiceErrorException(ERR_INVALID_REQUEST, "portonePaymentId는 필수입니다");
        }

        if (this.portonePaymentId != null && !Objects.equals(portonePaymentId, this.portonePaymentId)) {
            throw new ServiceErrorException(ERR_PORTONE_PAYMENT_ID_MISMATCH);
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
                throw new ServiceErrorException(ERR_PORTONE_PAYMENT_ID_MISMATCH);
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
            throw new ServiceErrorException(ERR_PAYMENT_STATUS_TRANSITION_INVALID);
        }
    }
}
