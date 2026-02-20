package com.spartaifive.commercepayment.domain.order.entity;

import com.spartaifive.commercepayment.common.exception.ErrorCode;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOTE: mysql에서는 binary(16)으로 테이블이 생성되는 것을 확인 하였습니다.
    @UuidGenerator
    @Column(unique = true, updatable = false, nullable = false)
    private UUID orderNumber;

    @NotNull
    @Min(0)
    // 이렇게 된다면 9,999,999,999,999.99 원이 저희 쇼핑몰의 최대 금액이 됩니다.
    @Column(precision = 15, scale = 2, nullable = false) 
    private BigDecimal totalPrice;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    // NOTE: 저희는 상품 삭제는 고려하지 않음으로 nullable = false
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public Order (
        BigDecimal totalPrice,
        User user
    ) {
        Objects.requireNonNull(totalPrice);
        Objects.requireNonNull(user);

        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_INVALID_ORDER_PRICE,
                    String.format("가격을 %s로 설정할려고 합니다. 가격은 음수 일 수 없습니다", totalPrice)
            );
        }

        this.totalPrice = totalPrice;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.user = user;
    }

    public void setStatusToRefund() {
        if (this.status != OrderStatus.COMPLETED) {
            throw new ServiceErrorException(
                ErrorCode.ERR_INVALID_ORDER_STATUS,
                String.format("%s상태에서 %s상태로 바꿀 수 는 없습니다",
                    this.status, OrderStatus.REFUNDED)
            );
        }

        this.status = OrderStatus.REFUNDED;
    }

    public void setStatusToCompleted() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new ServiceErrorException(
                ErrorCode.ERR_INVALID_ORDER_STATUS,
                String.format("%s상태에서 %s상태로 바꿀 수 는 없습니다",
                    this.status, OrderStatus.COMPLETED)
            );
        }

        this.status = OrderStatus.COMPLETED;
    }

    public void updateStatusForce(OrderStatus status) {
        this.status = Objects.requireNonNull(status);
    }
}
