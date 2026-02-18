package com.spartaifive.commercepayment.domain.point.repository;

import com.spartaifive.commercepayment.domain.point.dto.MembershipUpdateInfo;
import com.spartaifive.commercepayment.domain.point.dto.PointUpdateInfo;
import com.spartaifive.commercepayment.domain.point.dto.UserAndReadyPointsTotal;
import com.spartaifive.commercepayment.domain.point.dto.UserAndNotReadyPointsInfo;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.point.entity.PointStatus;
import com.spartaifive.commercepayment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findPointByOwnerUser(User ownerUser);

    List<Point> findPointByOwnerUser_Id(Long ownerUserId);

    @Query(
            "SELECT p FROM Point p WHERE p.pointStatus = com.spartaifive.commercepayment.domain.point.entity.PointStatus.CAN_BE_SPENT " +
            "AND p.ownerUser.id = :ownerUserId " +
            "AND p.pointRemaining > 0 " +
            "order by p.createdAt ASC"
    )
    List<Point> findPointsThatCanBeSpentSortedByCreatedAt(@Param("ownerUserId") Long ownerUserId);

    List<Point> findByOwnerUser_IdAndPointStatusAndParentPayment_Id(
            Long ownerUserId, PointStatus pointStatus, Long parentPaymentId);

    default List<Point> getPointsToVoidPerUserAndPayment(Long ownerUserId, Long parentPaymentId) {
        return this.findByOwnerUser_IdAndPointStatusAndParentPayment_Id(
                ownerUserId, PointStatus.NOT_READY_TO_BE_SPENT, parentPaymentId);
    }

    @Query(
            "SELECT new com.spartaifive.commercepayment.domain.point.dto.PointUpdateInfo(p, pay, membership) from Point p " +
            "JOIN FETCH p.parentPayment pay " +
            "JOIN p.ownerUser.membershipGrade membership " +
            "WHERE p.pointStatus = com.spartaifive.commercepayment.domain.point.entity.PointStatus.NOT_READY_TO_BE_SPENT AND " +
            "pay.paidAt < :paymentConfirmDay AND " +
            "pay.paymentStatus = com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus.PAID AND " +
            "p.ownerUser.id IN :userIds"
    )
    List<PointUpdateInfo> getPointUpdateInfos(
            @Param("userIds") Collection<Long> userIds, @Param("paymentConfirmDay")LocalDateTime paymentConfirmDay);

    @Query(
            "SELECT new com.spartaifive.commercepayment.domain.point.dto.MembershipUpdateInfo(u, COALESCE (SUM(pay.actualAmount), 0)) from User u " +
            "LEFT JOIN Payment pay on u.id = pay.userId " +
            "where u.id in :userIds AND " +
            "pay.paidAt < :paymentConfirmDay AND " +
            "pay.paymentStatus = com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus.PAID " +
            "group by u.id"
    )
    List<MembershipUpdateInfo> getMembershipUpdateInfo(
            @Param("userIds") Collection<Long> userIds, @Param("paymentConfirmDay")LocalDateTime paymentConfirmDay);

    @Query(
            "SELECT new com.spartaifive.commercepayment.domain.point.dto.UserAndReadyPointsTotal(u.id, COALESCE (SUM(p.pointRemaining), 0)) from User u " +
            "LEFT JOIN Point p on u.id = p.ownerUser.id " +
            "AND p.pointStatus = com.spartaifive.commercepayment.domain.point.entity.PointStatus.CAN_BE_SPENT " +
            "WHERE u.id IN :userIds " +
            "GROUP BY u.id"
    )
    List<UserAndReadyPointsTotal> getUserAndReadyPointsTotal(@Param("userIds") Collection<Long> userIds);

    @Query(
            "SELECT new com.spartaifive.commercepayment.domain.point.dto.UserAndNotReadyPointsInfo(u.id, m.rate, COALESCE(SUM(pay.actualAmount), 0)) from User u " +
            "LEFT JOIN Point p on u.id = p.ownerUser.id " +
            "AND p.pointStatus = com.spartaifive.commercepayment.domain.point.entity.PointStatus.NOT_READY_TO_BE_SPENT " +
            "LEFT JOIN MembershipGrade m on u.membershipGrade.id = m.id " +
            "LEFT JOIN Payment pay on p.parentPayment.id = pay.id " +
            "WHERE u.id IN :userIds " +
            "GROUP BY u.id"
    )
    List<UserAndNotReadyPointsInfo> getUserAndNotReadyPointsInfo(@Param("userIds") Collection<Long> userIds);
}
