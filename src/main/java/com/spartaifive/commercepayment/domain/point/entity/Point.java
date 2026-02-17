package com.spartaifive.commercepayment.domain.point.entity;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    PointStatus pointStatus;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "parent_payment_id")
    Payment parentPayment;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "parent_order_id")
    Order parentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "owner_user_id")
    User ownerUser;

    // TODO: 왜 nullable인지 설명하기
    @Column(precision = 10, scale = 2, nullable = true)
    BigDecimal originalPointAmount;
    @Column(precision = 10, scale = 2, nullable = true)
    BigDecimal pointRemaining;

    @NotNull
    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime modifiedAt;

    public Point(
            Payment parentPayment,
            Order parentOrder,
            User ownerUser
    ) {
        this.parentPayment = Objects.requireNonNull(parentPayment);
        this.parentOrder = Objects.requireNonNull(parentOrder);
        this.ownerUser = Objects.requireNonNull(ownerUser);

        this.pointStatus = PointStatus.NOT_READY_TO_BE_SPENT;
    }

    public void updatePointStatus(PointStatus status) {
        this.pointStatus = status;
    }

    public void initPointAmount(BigDecimal amount) {
        this.originalPointAmount = amount;
        this.pointRemaining = amount;
    }

    public void updatePointRemaining(BigDecimal newPointRemaining) {
        if (this.originalPointAmount == null) {
            throw new IllegalStateException("포인트는 잔액은 원래 포인트가 있어야만 업데이트 할 수 있습니다");
        }
        newPointRemaining = Objects.requireNonNull(newPointRemaining);

        if (newPointRemaining.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "새 포인트 잔액이 음수 입니다"
            );
        }

        if (newPointRemaining.compareTo(this.originalPointAmount) > 0) {
            throw new IllegalStateException(
                    String.format("새 포인트 잔액(%s)이 원래 포인트 작액(%s) 보다 큽니다", newPointRemaining, this.originalPointAmount)
            );
        }

        this.pointRemaining =  newPointRemaining;
    }
}
