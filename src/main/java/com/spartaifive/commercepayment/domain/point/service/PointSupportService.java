package com.spartaifive.commercepayment.domain.point.service;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointSupportService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;
    private final PointAuditRepository pointAuditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserMembership(
            Long userId,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException(String.format(
                        "%s id의 고객을 찾지 못했습니다",userId))
        );

        List<Payment> payments = paymentRepository.findByUserId(user.getId());

        BigDecimal confirmedPaymentTotal = BigDecimal.ZERO;

        for (Payment p : payments) {
            if (
                    p.getPaymentStatus().equals(PaymentStatus.PAID) &&
                    p.getPaidAt().isBefore(paymentConfirmDay)
            ) {
                confirmedPaymentTotal = confirmedPaymentTotal.add(p.getActualAmount());
            }
        }

        MembershipGrade userMembership = null;

        for (MembershipGrade membershipGrade : membershipGrades) {
            if (confirmedPaymentTotal.compareTo(membershipGrade.getRequiredPurchaseAmount()) <= 0) {
                userMembership = membershipGrade;
                break;
            }
        }

        if (userMembership != null) {
            user.updateMembership(userMembership);
            userRepository.save(user);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserPoints(
            Long userId,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException(String.format(
                        "%s id의 고객을 찾지 못했습니다",userId))
        );

        MembershipGrade membership = user.getMembershipGrade();
        if (membership == null) {
            return;
        }

        List<Point> points = pointRepository.findPointByOwnerUser(user);
        List<PointAudit> audits = new ArrayList<>();

        for (Point point : points) {
            if (
                    point.getPointStatus().equals(PointStatus.NOT_READY_TO_BE_SPENT) &&
                    point.getParentPayment().getPaidAt().isBefore(paymentConfirmDay)
            ) {
                BigDecimal pointAmount = getPointAmountPerPurchase(
                        point.getParentPayment().getActualAmount(),
                        membership.getRate()
                );
                point.initPointAmount(pointAmount);
                point.updatePointStatus(PointStatus.CAN_BE_SPENT);

                PointAudit audit = new PointAudit(
                        user,
                        point.getParentOrder(),
                        point.getParentPayment(),
                        point,
                        PointAuditType.POINT_BECAME_READY,
                        pointAmount
                );

                audits.add(audit);
            }
        }

        pointRepository.saveAll(points);
        pointAuditRepository.saveAll(audits);
    }

    public BigDecimal getPointAmountPerPurchase(
            BigDecimal paymentAmount,
            Long rate
    ) {
        BigDecimal oneHundred = BigDecimal.valueOf(100);
        BigDecimal point = paymentAmount.multiply(BigDecimal.valueOf(rate));
        point = point.divide(oneHundred, 2, RoundingMode.HALF_UP);

        return point;
    }
}
