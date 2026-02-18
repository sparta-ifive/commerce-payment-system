package com.spartaifive.commercepayment.domain.point.service;

import com.spartaifive.commercepayment.common.exception.ErrorCode;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import com.spartaifive.commercepayment.domain.point.dto.MembershipUpdateInfo;
import com.spartaifive.commercepayment.domain.point.dto.PointUpdateInfo;
import com.spartaifive.commercepayment.domain.point.dto.UserAndReadyPointsTotal;
import com.spartaifive.commercepayment.domain.point.dto.UserAndNotReadyPointsInfo;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.point.entity.PointAudit;
import com.spartaifive.commercepayment.domain.point.entity.PointAuditType;
import com.spartaifive.commercepayment.domain.point.entity.PointStatus;
import com.spartaifive.commercepayment.domain.point.repository.PointAuditRepository;
import com.spartaifive.commercepayment.domain.point.repository.PointRepository;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointSupportService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;
    private final OrderRepository orderRepository;
    private final PointAuditRepository pointAuditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserMembership(
            List<Long> userIds,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        List<MembershipUpdateInfo> updateInfos = pointRepository.getMembershipUpdateInfo(
                userIds, paymentConfirmDay);

        List<User> users = new ArrayList<>();

        for (MembershipUpdateInfo info : updateInfos) {
            User user = info.user();

            MembershipGrade userMembership = null;

            for (MembershipGrade membershipGrade : membershipGrades) {
                if (info.confirmedPaymentTotal().compareTo(membershipGrade.getRequiredPurchaseAmount()) <= 0) {
                    userMembership = membershipGrade;
                    break;
                }
            }

            if (userMembership != null) {
                user.updateMembership(userMembership);
                users.add(user);
            }
        }

        userRepository.saveAll(users);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserPoints(
            List<Long> userIds,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        List<PointUpdateInfo> updateInfos = pointRepository.getPointUpdateInfos(
                userIds, paymentConfirmDay);

        List<Point> pointsToSave = new ArrayList<>();
        List<PointAudit> audits = new ArrayList<>();
        
        for (PointUpdateInfo info : updateInfos) {
            if (info.membershipGrade() == null) {
                continue;
            }

            Point point = info.point();
            MembershipGrade membership = info.membershipGrade();
            Payment payment = info.payment();

            BigDecimal pointAmount = getPointAmountPerPurchase(
                    point.getParentPayment().getActualAmount(),
                    membership.getRate()
            );

            point.initPointAmount(pointAmount);
            point.updatePointStatus(PointStatus.CAN_BE_SPENT);

            PointAudit audit = new PointAudit(
                    userRepository.getReferenceById(point.getOwnerUser().getId()),
                    orderRepository.getReferenceById(point.getParentOrder().getId()),
                    payment,
                    point,
                    PointAuditType.POINT_BECAME_READY,
                    pointAmount
            );

            audits.add(audit);
            pointsToSave.add(point);
        }

        pointRepository.saveAll(pointsToSave);
        pointAuditRepository.saveAll(audits);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserPointsTotal(
            List<Long> userIds
    ) {
        Map<Long, User> idToUser = new HashMap<>();

        List<User> users = userRepository.findAllUsersByIds(userIds);

        for (User user : users) {
            idToUser.put(user.getId(), user);
        }

        List<UserAndReadyPointsTotal> pointTotals = pointRepository.getUserAndReadyPointsTotal(userIds);

        for (UserAndReadyPointsTotal total : pointTotals)  {
            User user = idToUser.get(total.userId());
            if (user != null) {
                user.updatePointsReadyToSpendClamped(total.amount());
            }
        }

        List<UserAndNotReadyPointsInfo> infos = pointRepository.getUserAndNotReadyPointsInfo(userIds);

        for (UserAndNotReadyPointsInfo info : infos) {
            User user = idToUser.get(info.userId());
            if (user != null) {
                 BigDecimal pointAmount = getPointAmountPerPurchase(
                         info.paymentTotal(),
                         info.membershipRate()
                 );

                 user.updatePointsNotReadyToSpendClamped(pointAmount);
            }
        }

        userRepository.saveAll(users);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateUserPoints(
            Long userId, 
            boolean confirmedOnly
    ) {
        List<UserAndReadyPointsTotal> totals = pointRepository.getUserAndReadyPointsTotal(List.of(userId));

        if (!(totals.size() == 1 && totals.get(0).userId().equals(userId))) {
            throw new ServiceErrorException(ErrorCode.ERR_POINT_FAILED_TO_CALCULATE_TOTAL);
        }

        if (confirmedOnly) {
            return totals.get(0).amount();
        }

        List<UserAndNotReadyPointsInfo> infos = pointRepository.getUserAndNotReadyPointsInfo(List.of(userId));

        if (!(infos.size() == 1 && infos.get(0).userId().equals(userId))) {
            throw new ServiceErrorException(ErrorCode.ERR_POINT_FAILED_TO_CALCULATE_TOTAL);
        }

        BigDecimal pointsNotConfirmed = getPointAmountPerPurchase(
            infos.get(0).paymentTotal(),
            infos.get(0).membershipRate()
        );

        return totals.get(0).amount().add(pointsNotConfirmed);
    }

    // ============
    // UTIL들
    // ============

    public static BigDecimal getPointAmountPerPurchase(
            BigDecimal paymentAmount,
            Long rate
    ) {
        BigDecimal oneHundred = BigDecimal.valueOf(100);
        BigDecimal point = paymentAmount.multiply(BigDecimal.valueOf(rate));
        point = point.divide(oneHundred, 2, RoundingMode.HALF_UP);

        return point;
    }

    public static List<PointDecrease> decreasePoints(
            List<Point> points,
            BigDecimal pointToSpend
    ) {
        List<PointDecrease> decreases = new ArrayList<>();

        for (Point point : points) {
            BigDecimal from = point.getPointRemaining();
            BigDecimal to;

            boolean doBreak = false;

            if (point.getPointRemaining().compareTo(pointToSpend) < 0) {
                pointToSpend = pointToSpend.subtract(point.getPointRemaining());
                point.updatePointRemaining(BigDecimal.ZERO);
                
                to = BigDecimal.ZERO;
            } else {
                BigDecimal newPointRemaining = point.getPointRemaining();
                newPointRemaining = newPointRemaining.subtract(pointToSpend);
                point.updatePointRemaining(newPointRemaining);

                to = newPointRemaining;

                doBreak = true;
            }
            
            decreases.add(new PointDecrease(point, from, to));

            if (doBreak) {
                break;
            }
        }

        return decreases;
    }

    public static record PointDecrease (
            Point point,
            BigDecimal from,
            BigDecimal to
    ) {}
}
