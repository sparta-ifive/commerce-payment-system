package com.spartaifive.commercepayment.domain.user.entity;

import com.spartaifive.commercepayment.common.service.TimeService;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
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

    // 아래 두 필드는 유저가 갖고 있는 포인트 총량을 나타냅니다.
    // 그리고 장담컨데 저희가 뭘하던 틀릴 것입니다.
    // 정말로 단순히 유저가 포인트를 얼마 가지고 있는지 알려주고 싶을때 쓸려고 저장하는 값입니다.
    // 결제시에는 **절대** 믿지 마세요
    @Column(name = "points_ready_to_spend", nullable = false, precision = 15, scale = 2)
    @Min(0)
    private BigDecimal pointsReadyToSpend;

    @Column(name = "points_not_ready_to_spend", nullable = false, precision = 15, scale = 2)
    @Min(0)
    private BigDecimal pointsNotReadyToSpend;


    public static User create(
            MembershipGrade membershipGrade,
            String name,
            String email,
            String encodedPassword,
            String phone
    ) {
        LocalDateTime now = TimeService.getCurrentTime();

        return User.builder()
                .membershipGrade(membershipGrade)
                .name(name)
                .email(email)
                .password(encodedPassword)
                .phone(phone)
                .createdAt(now)
                .membershipUpdatedDate(now)
                .pointsReadyToSpend(BigDecimal.ZERO)
                .pointsNotReadyToSpend(BigDecimal.ZERO)
                .build();
    }

    public void updateMembership(MembershipGrade membershipGrade) {
        Objects.requireNonNull(membershipGrade);
        this.membershipUpdatedDate = TimeService.getCurrentTime();
        this.membershipGrade = membershipGrade;
    }

    /**
     * 사용 가능한 포인트를 update 합니다.
     * 0 미만으로 설정 할려고 무시하고 false를 돌려줍니다.
     * @param toSet 새 포인트 값
     * @return update 성공 실패 여부
     */
    public boolean updatePointsReadyToSpend(BigDecimal toSet) {
        if (toSet.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        this.pointsReadyToSpend = toSet;
        return true;
    }

    /**
     * 사용 불가능한 포인트를 update 합니다.
     * 0 미만으로 설정 할려고 무시하고 false를 돌려줍니다.
     * @param toSet 새 포인트 값
     * @return update 성공 실패 여부
     */
    public boolean updatePointsNotReadyToSpend(BigDecimal toSet) {
        if (toSet.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        this.pointsNotReadyToSpend = toSet;
        return true;
    }

    /**
     * 사용 가능한 포인트를 update 합니다.
     * 0 미만으로 설정 할려고 할경우 0으로 설정합니다
     * @param toSet 새 포인트 값
     * @return clamping을 했는지 안했는지
     */
    public boolean updatePointsReadyToSpendClamped(BigDecimal toSet) {
        boolean clampedToZero = false;
        if (toSet.compareTo(BigDecimal.ZERO) < 0) {
            toSet = BigDecimal.ZERO;
            clampedToZero = true;
        }
        this.pointsReadyToSpend = toSet;
        return clampedToZero;
    }

    /**
     * 사용 불가능한 포인트를 update 합니다.
     * 0 미만으로 설정 할려고 할경우 0으로 설정합니다
     * @return clamping을 했는지 안했는지
     */
    public boolean updatePointsNotReadyToSpendClamped(BigDecimal toSet) {
        boolean clampedToZero = false;
        if (toSet.compareTo(BigDecimal.ZERO) < 0) {
            toSet = BigDecimal.ZERO;
            clampedToZero = true;
        }
        this.pointsNotReadyToSpend = toSet;
        return clampedToZero;
    }
}
