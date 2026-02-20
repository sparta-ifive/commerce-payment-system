package com.spartaifive.commercepayment.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "membership_grades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MembershipGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long id;

    @Column(name = "membership_name", nullable = false, length = 64)
    private String name; // NORMAL, VIP, VVIP

    @Column(name = "membership_rate", nullable = false)
    private Long rate; // 적립률

    @Column(
            name = "required_purchase_amount",
            precision = 15,
            scale = 2,
            nullable = false
    )
    private BigDecimal requiredPurchaseAmount;

    public MembershipGrade(
            String name,
            Long rate,
            BigDecimal requiredPurchaseAmount) {
        this.name = name;
        this.rate = rate;
        this.requiredPurchaseAmount = requiredPurchaseAmount;
    }
}
