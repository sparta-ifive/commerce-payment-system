package com.spartaifive.commercepayment.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.spartaifive.commercepayment.domain.order.entity.Order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOTE: 저희는 주문 삭제는 고려하지 않음으로 nullable = false
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id")
    private Order order;

    @NotNull
    @Column(nullable = false)
    private String paymentId;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @NotNull
    // 이렇게 된다면 99,999,999.99 원이 저희 쇼핑몰의 최대 금액이 됩니다.
    @Column(precision = 10, scale = 2, nullable = false) 
    private BigDecimal payAmount;

    @Column(nullable = true)
    private LocalDateTime paidAt;

    @Column(nullable = true)
    private LocalDateTime refundedAt;

    public Payment(Order order, String paymentId, BigDecimal payAmount) {
        this.order = order;
        this.paymentId = paymentId;
        this.status = PaymentStatus.READY;
        this.payAmount = payAmount;
    }
}
