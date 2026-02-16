package com.spartaifive.commercepayment.domain.point.service;

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

    // TODO: N+1을 막기 위해서 entity를 받을 수 도 있지만 일단은
    // 정상적으로 작동 하는 것을 확인 하는 것이 먼저기 때문에 성능은 포기하고
    // id를 받습니다.
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
            throw new RuntimeException("포인트는 확정된 결제로만 생성됩니다.");
        }

        // 포인트 생성시 주문은 완료여야 합니다.
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new RuntimeException("포인트는 확정된 주문에서만 생성됩니다.");
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

        pointRepository.save(point);
        pointAuditRepository.save(audit);
    }

    // TODO: N+1을 막기 위해서 entity를 받을 수 도 있지만 일단은
    // 정상적으로 작동 하는 것을 확인 하는 것이 먼저기 때문에 성능은 포기하고
    // id를 받습니다.
    @Transactional(readOnly = true)
    public BigDecimal validatedAndSubtractPointFromOrderTotalPrice(
            Long userId,
            BigDecimal orderTotalPrice,
            BigDecimal pointAmount
    ) {
        if (pointAmount.compareTo(orderTotalPrice) > 0) {
            throw new IllegalArgumentException("주문 총액보다 포인트를 더 사용할 수는 없습니다");
        }

        BigDecimal userPoints = pointSupportService.calculateUserPoints(userId, true);

        if (pointAmount.compareTo(userPoints) > 0) {
            throw new IllegalArgumentException(
                    String.format("유저의 잔액 포인트(%s)가 부족하여 (%s)의 포인트를 사용할 수 없습니다.", userPoints, pointAmount)
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

        // 포인트 생성시 결제는 완료여야 합니다.
        if (!payment.getPaymentStatus().equals(PaymentStatus.PAID)) {
            throw new RuntimeException("포인트는 확정된 결제로만 생성됩니다.");
        }

        // 포인트 생성시 주문은 완료여야 합니다.
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new RuntimeException("포인트는 확정된 주문에서만 생성됩니다.");
        }

        // 유저한테 쓸 포인트가 있긴 한지 확인
        BigDecimal userPoints = pointSupportService.calculateUserPoints(userId, true);

        if (userPoints.compareTo(pointToSpend) < 0) {
            throw new IllegalArgumentException(
                    String.format("유저의 잔액 포인트(%s)가 부족하여 (%s)의 포인트를 사용할 수 없습니다.", userPoints, pointToSpend)
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
    }

    @Transactional()
    public void voidPoints(
            Long paymentId,
            Long orderId,
            Long userId
    ) {
        Payment payment = paymentRepository.getReferenceById(paymentId);

        Order order = orderRepository.getReferenceById(orderId);

        User user = userRepository.getReferenceById(userId);

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
    }

    private Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> 
                        new IllegalStateException(String.format("%s 아이디의 결제는 존재하지 않습니다", paymentId)));
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> 
                        new IllegalStateException(String.format("%s 아이디의 주문은 존재하지 않습니다", orderId)));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> 
                        new IllegalStateException(String.format("%s 아이디의 고객은 존재하지 않습니다", userId)));
    }
}
