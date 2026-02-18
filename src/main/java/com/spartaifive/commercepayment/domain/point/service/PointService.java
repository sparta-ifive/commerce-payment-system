package com.spartaifive.commercepayment.domain.point.service;

import com.spartaifive.commercepayment.common.exception.ErrorCode;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import com.spartaifive.commercepayment.domain.point.entity.PointAudit;
import com.spartaifive.commercepayment.domain.point.entity.PointAuditType;
import com.spartaifive.commercepayment.domain.point.entity.PointStatus;
import com.spartaifive.commercepayment.domain.point.repository.PointAuditRepository;
import com.spartaifive.commercepayment.domain.point.repository.PointRepository;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointAuditRepository pointAuditRepository;
    private final PointSupportService pointSupportService;

    // 이 서비스의 메소드들이 엔티티 대신에 엔티티 ID를 받는 이유는
    // 미래에 이 Transaction들이 @Transactional(propagation = Propagation.REQUIRES_NEW)
    // 로 바뀔 수도 있다고 생각하기 때문입니다.
    //
    // 그럴시 entity를 받을경우 PersistentObjectException이 일어 날 수 있기 때문에 ID로 일단 받습니다.

    @Transactional
    public void createPointAfterPaymentConfirm(
            Long paymentId,
            Long orderId,
            Long userId
    ) {
        Payment payment = getPaymentById(paymentId);

        Order order = getOrderById(orderId);

        User user = getUserById(userId);

        // 포인트 생성시 결제는 완료여야 합니다.
        if (!payment.getPaymentStatus().equals(PaymentStatus.PAID)) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_FAILED_TO_CREATE_POINT, 
                    "포인트는 확정된 결제에서만 생성 가능합니다");
        }

        // 포인트 생성시 주문은 완료여야 합니다.
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_FAILED_TO_CREATE_POINT, 
                    "포인트는 확정된 주문에서만 생성 가능합니다");
        }

        Point point = new Point(
                payment,
                order,
                user
        );

        PointAudit audit = new PointAudit(
                user,
                order,
                payment,
                point,
                PointAuditType.POINT_CREATED
        );

        // 유저의 사용 불가 포인트 증가
        {
            BigDecimal currentPoints = user.getPointsNotReadyToSpend();
            BigDecimal toAdd = PointSupportService.getPointAmountPerPurchase(
                    payment.getActualAmount(),
                    user.getMembershipGrade().getRate()
            );

            user.updatePointsNotReadyToSpendClamped(currentPoints.add(toAdd));
        }

        pointRepository.save(point);
        pointAuditRepository.save(audit);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal validatedAndSubtractPointFromOrderTotalPrice(
            Long userId,
            BigDecimal orderTotalPrice,
            BigDecimal pointAmount
    ) {
        if (pointAmount.compareTo(orderTotalPrice) > 0) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_POINT_EXCEEDS_PAYMENT
            );
        }

        BigDecimal userPoints = pointSupportService.calculateUserPoints(userId, true);

        if (pointAmount.compareTo(userPoints) > 0) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_INSUFFICIENT_POINT,
                    String.format("유저의 잔액 포인트(%s)가 부족하여 (%s)의 포인트를 사용할 수 없습니다",
                        userPoints, pointAmount)
            );
        }

        return orderTotalPrice.subtract(pointAmount);
    }

    @Transactional()
    public void spendPoint(
            Long paymentId,
            Long orderId,
            Long userId
    ) {
        Payment payment = getPaymentById(paymentId);

        // 이 결제는 포인트를 쓸게 아니므로 돌려주기
        if (payment.getPointToSpend() == null) {
            return;
        }

        BigDecimal pointToSpend = payment.getPointToSpend();

        // 결제가 0 포인트를 쓰고 싶다고 하면 그냥 돌려주기
        if (pointToSpend.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Order order = getOrderById(orderId);

        User user = getUserById(userId);

        // 포인트 소비시 결제는 완료여야 합니다.
        if (!payment.getPaymentStatus().equals(PaymentStatus.PAID)) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_FAILED_TO_SPEND_POINT, 
                    "포인트는 확정된 결제에서만 소비 가능합니다");
        }

        // 포인트 소비시 주문은 완료여야 합니다.
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_FAILED_TO_SPEND_POINT, 
                    "포인트는 확정된 주문에서만 소비 가능합니다");
        }

        // 유저한테 쓸 포인트가 있긴 한지 확인
        BigDecimal userPoints = pointSupportService.calculateUserPoints(userId, true);

        if (userPoints.compareTo(pointToSpend) < 0) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_POINT_INSUFFICIENT_POINT,
                    String.format("유저의 잔액 포인트(%s)가 부족하여 (%s)의 포인트를 사용할 수 없습니다",
                        userPoints, pointToSpend)
            );
        }

        List<Point> points = pointRepository.findPointsThatCanBeSpentSortedByCreatedAt(user.getId());

        // 포인트를 줄임
        List<PointSupportService.PointDecrease> decreases = 
            pointSupportService.decreasePoints(points, pointToSpend);

        pointRepository.saveAll(decreases.stream().map(x -> x.point()).toList());

        List<PointAudit> pointAudits = new ArrayList<>();

        // 포인트를 줄인 사실 기록
        for (PointSupportService.PointDecrease decrease : decreases) {
            PointAudit audit = new PointAudit(
                    user,
                    order,
                    payment,
                    decrease.point(),
                    PointAuditType.POINT_SPENT,
                    decrease.from().subtract(decrease.to())
            );

            pointAudits.add(audit);
        }

        pointAuditRepository.saveAll(pointAudits);

        // 고객의 총 사용 가능 포인트 양을 업데이트
        {
            BigDecimal newAmount = userPoints.subtract(pointToSpend);
            if (user.updatePointsReadyToSpendClamped(newAmount)) {
                log.error("[POINT_SERVICE]: tried to set user {} pointsReadyToSpend below zero", user.getId());
            }
            userRepository.save(user);
        }
    }

    @Transactional()
    public void voidPoints(
            Long paymentId,
            Long orderId,
            Long userId
    ) {
        Payment payment = getPaymentById(paymentId);

        Order order = orderRepository.getReferenceById(orderId);

        User user = getUserById(userId);

        List<Point> points = pointRepository.getPointsToVoidPerUserAndPayment(userId, paymentId);
        List<PointAudit> pointAudits = new ArrayList<>();

        for (Point point : points) {
            point.updatePointStatus(PointStatus.VOIDED);

            PointAudit audit = new PointAudit(
                    user,
                    order,
                    payment,
                    point,
                    PointAuditType.POINT_VOIDED
            );

            pointAudits.add(audit);
        }

        pointAuditRepository.saveAll(pointAudits);
        pointRepository.saveAll(points);

        // 고객의 총 사용 불가능 포인트를 감소
        {
            BigDecimal currentPoints = user.getPointsNotReadyToSpend();
            BigDecimal toSubtract = PointSupportService.getPointAmountPerPurchase(
                    payment.getActualAmount(),
                    user.getMembershipGrade().getRate()
            );

            if (user.updatePointsNotReadyToSpendClamped(currentPoints.subtract(toSubtract))) {
                log.error("[POINT_SERVICE]: tried to set user {} pointsReadyToSpend below zero", user.getId());
            }
        }
    }

    private Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> 
                        new ServiceErrorException(ErrorCode.ERR_PAYMENT_NOT_FOUND));
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> 
                        new ServiceErrorException(ErrorCode.ERR_ORDER_NOT_FOUND));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> 
                        new ServiceErrorException(ErrorCode.ERR_USER_NOT_FOUND));
    }
}
