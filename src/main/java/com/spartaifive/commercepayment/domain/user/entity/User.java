package com.spartaifive.commercepayment.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 멤버십 등급 FK값
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private MembershipGrade membershipGrade;

    @Column(name = "user_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "membership_updated_date", nullable = false)
    private LocalDateTime membershipUpdatedDate;

    @Column(name = "total_point", nullable = false)
    private Integer totalPoint;

    @Column(name = "total_paid_amount", nullable = false)
    private Integer totalPaidAmount;


    public static User create(
            MembershipGrade membershipGrade,
            String name,
            String email,
            String encodedPassword,
            String phone
    ) {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .membershipGrade(membershipGrade)
                .name(name)
                .email(email)
                .password(encodedPassword)
                .phone(phone)
                .createdAt(now)
                .membershipUpdatedDate(now)
                .totalPoint(0)
                .totalPaidAmount(0)
                .build();
    }

    public void updateMembership(MembershipGrade membershipGrade) {
        Objects.requireNonNull(membershipGrade);
        this.membershipUpdatedDate = LocalDateTime.now();
        this.membershipGrade = membershipGrade;
    }
}
