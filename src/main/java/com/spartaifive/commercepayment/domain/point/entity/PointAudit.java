package com.spartaifive.commercepayment.domain.point.entity;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "point_audits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PointAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    //TODO: 정말 여기에 모든게 ManyToOne이 맞나?
    //TODO: 정말 여기에 nullable이 맞는지?

    // 뭔가 auditing에 fk check를 하는게 이상한거 같기도 하고,
    // 또 나중에 참고용으로 넣는 것도 좋을 거 같아서 fk check를 끕니다.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "payment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "point_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    Point point;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    PointAuditType pointAuditType;

    // point는 생성시 포인트이 양이 정해져 있지 않습니다.
    // 이를 반영하고자 point audit도 양이 nullable입니다.
    @Column(precision = 10, scale = 2, nullable = true)
    @Min(0)
    BigDecimal amount;

    @NotNull
    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    public PointAudit(
            User user,
            Order order,
            Payment payment,
            Point point,
            PointAuditType auditType
    ) {
        this.user = user;
        this.order = order;
        this.payment = payment;
        this.point = point;
        this.pointAuditType = auditType;
    }

    public PointAudit(
            User user,
            Order order,
            Payment payment,
            Point point,
            PointAuditType auditType,
            BigDecimal amount
    ) {
        this.user = user;
        this.order = order;
        this.payment = payment;
        this.point = point;
        this.pointAuditType = auditType;
        this.amount = amount;
    }
}
