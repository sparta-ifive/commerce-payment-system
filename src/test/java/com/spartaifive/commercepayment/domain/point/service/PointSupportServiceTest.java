package com.spartaifive.commercepayment.domain.point.service;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RequiredArgsConstructor
public class PointSupportServiceTest {

    @Test
    public void 포인트_감소_로직() {
        // ===============
        // GIVEN
        // ===============

        // 대충 만든 fixture
        MembershipGrade membership = new MembershipGrade("NORMAL", 20L, BigDecimal.valueOf(5000));

        User user = User.create(
                membership,
                "김희찬",
                "user1@gmail.com",
                "1234qwer",
                "01011112222"
        );

        Order order = new Order(BigDecimal.valueOf(2000), user);

        Payment payment = Payment.createAttempt(
                1L, order, BigDecimal.valueOf(2000), BigDecimal.valueOf(1000), "fakeid"
        );

        Point point1 = new Point(payment, order, user);
        Point point2 = new Point(payment, order, user);
        Point point3 = new Point(payment, order, user);
        Point point4 = new Point(payment, order, user);

        point1.initPointAmount(BigDecimal.valueOf(100));
        point2.initPointAmount(BigDecimal.valueOf(200));
        point3.initPointAmount(BigDecimal.valueOf(300));
        point4.initPointAmount(BigDecimal.valueOf(400));

        // ===============
        // WHEN
        // ===============
        List<PointSupportService.PointDecrease> decreases = PointSupportService.decreasePoints(
                List.of(point1, point2, point3),
                BigDecimal.valueOf(400)
        );

        // ===============
        // THEN
        // ===============
        List<Point> decreasedPoints = decreases.stream().map(x -> x.point()).toList();

        assertThat(decreasedPoints).contains(point1, point2, point3);
        assertThat(decreasedPoints).doesNotContain(point4);

        assertThat(point1.getPointRemaining()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(point2.getPointRemaining()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(point3.getPointRemaining()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(point4.getPointRemaining()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }
}
