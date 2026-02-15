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
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointAuditRepository pointAuditRepository;
    private final PointSupportService pointSupportService;

    // N+1을 막기 위해서 entity를 받을 수 도 있지만 일단은
    // 정상적으로 작동 하는 것을 확인 하는 것이 먼저기 때문에 성능은 포기하고
    // id를 받습니다.
    @Transactional
    public void createPointAfterPaymentConfirm(
            Long paymentId,
            Long orderId,
            Long userId
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> 
                        new IllegalStateException(String.format("%s 아이디의 결제는 존재하지 않습니다", paymentId)));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> 
                        new IllegalStateException(String.format("%s 아이디의 주문은 존재하지 않습니다", orderId)));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> 
                        new IllegalStateException(String.format("%s 아이디의 고객은 존재하지 않습니다", userId)));

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

    @Transactional(readOnly = true)
    public BigDecimal getUserPoints(
            Long userId, 
            boolean confirmedOnly
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException(String.format(
                        "%s id의 고객을 찾지 못했습니다",userId))
        );

        List<Point> points = pointRepository.findPointByOwnerUser_Id(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (Point point : points) {
            if (point.getPointStatus().equals(PointStatus.CAN_BE_SPENT)) {
                total = total.add(point.getPointRemaining());
            }

            if (!confirmedOnly && point.getPointStatus().equals(PointStatus.NOT_READY_TO_BE_SPENT)) {
                BigDecimal paymentAmount = point.getParentPayment().getActualAmount();
                Long rate = user.getMembershipGrade().getRate();
                total = total.add(
                        pointSupportService.getPointAmountPerPurchase(paymentAmount, rate));
            }
        }

        return total;
    }
}
