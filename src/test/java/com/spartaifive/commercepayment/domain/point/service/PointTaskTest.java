package com.spartaifive.commercepayment.domain.point.service;

import com.spartaifive.commercepayment.common.constants.Constants;
import com.spartaifive.commercepayment.common.util.DatabaseCleaner;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.point.entity.PointAudit;
import com.spartaifive.commercepayment.domain.point.tasks.PointTasks;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.refund.entity.Refund;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.entity.UserRefreshToken;
import com.spartaifive.commercepayment.domain.user.repository.MembershipGradeRepository;
import com.spartaifive.commercepayment.domain.webhookevent.entity.Webhook;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@TestPropertySource(properties = {"spring.config.additional-location= classpath:test-h2-basic.yml"})
public class PointTaskTest {
    @PersistenceUnit
    private EntityManagerFactory emf;

    @Autowired
    private MembershipGradeRepository membershipGradeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PointService pointService;
    @Autowired
    private PointSupportService pointSupportService;

    @Autowired
    private PointTasks pointTasks;

    @Autowired
    DatabaseCleaner dbCleaner;

    @Autowired
    Constants constants;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    public void cleanup() {
        dbCleaner.deleteTables(
                OrderProduct.class,
                Order.class,
                Payment.class,
                PointAudit.class,
                Point.class,
                Product.class,
                UserRefreshToken.class,
                Refund.class,
                User.class,
                Webhook.class
        );
    }

    @RepeatedTest(2)
    public void 기본적인_포인트_멤버쉽_규칙() {
        // ===================
        // GIVEN
        // ===================

        LocalDateTime timeNow = LocalDateTime.now();

        // 환불 기간 이전으로 시간여행
        LocalDateTime timeBeforeRefund = timeNow.minus(constants.getRefundPeriod()).minusMinutes(1);

        Mockito.when(clock.instant()).thenReturn(timeBeforeRefund.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // user 생성
        User user1;
        User user2;
        User user3;
        User user4;

        {
            MembershipGrade membership = membershipGradeRepository.findByName("NORMAL").get();
            EntityManager em = emf.createEntityManager();

            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                user1 = User.create(
                        membership,
                        "김희찬",
                        "user1@gmail.com",
                        passwordEncoder.encode("1234qwer"),
                        "01011112222"
                );

                user2 = User.create(
                        membership,
                        "김이선",
                        "user2@gmail.com",
                        passwordEncoder.encode("1234qwer"),
                        "01033334444"
                );

                user3 = User.create(
                        membership,
                        "이착혁",
                        "user3@gmail.com",
                        passwordEncoder.encode("1234qwer"),
                        "01055556666"
                );

                user4 = User.create(
                        membership,
                        "조용필",
                        "user4@gmail.com",
                        passwordEncoder.encode("1234qwer"),
                        "01077778888"
                );

                em.persist(user1);
                em.persist(user2);
                em.persist(user3);
                em.persist(user4);
                em.flush();

                tx.commit();
            } finally {
                em.close();
            }
        }

        // 가짜 구매 내역 생성
        var orderPayment1 = createFakePurchase(user1, BigDecimal.valueOf(1000), timeBeforeRefund);
        var orderPayment2 = createFakePurchase(user2, BigDecimal.valueOf(50000), timeBeforeRefund);
        var orderPayment3 = createFakePurchase(user3, BigDecimal.valueOf(100000), timeBeforeRefund);
        var orderPayment4 = createFakePurchase(user4, BigDecimal.valueOf(150000), timeBeforeRefund);

        // ===================
        // WHEN
        // ===================
        
        // 포인트 생성
        pointService.createPointAfterPaymentConfirm(
                orderPayment1.getSecond().getId(),
                orderPayment1.getFirst().getId(),
                user1.getId()
        );

        pointService.createPointAfterPaymentConfirm(
                orderPayment2.getSecond().getId(),
                orderPayment2.getFirst().getId(),
                user2.getId()
        );

        pointService.createPointAfterPaymentConfirm(
                orderPayment3.getSecond().getId(),
                orderPayment3.getFirst().getId(),
                user3.getId()
        );

        pointService.createPointAfterPaymentConfirm(
                orderPayment4.getSecond().getId(),
                orderPayment4.getFirst().getId(),
                user4.getId()
        );

        // ===================
        // THEN
        // ===================

        // 현재로 돌아옴
        Mockito.when(clock.instant()).thenReturn(timeNow.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // 포인트와 멤버쉽이 맞는지 확인
        pointTasks.calculateMembershipAndReadyPoints();

        {
            EntityManager em = emf.createEntityManager();
            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                user1 = em.find(User.class, Long.valueOf(user1.getId()));
                user2 = em.find(User.class, Long.valueOf(user2.getId()));
                user3 = em.find(User.class, Long.valueOf(user3.getId()));
                user4 = em.find(User.class, Long.valueOf(user4.getId()));

                assertThat(user1.getMembershipGrade().getName()).isEqualTo(membershipGradeRepository.findByName("NORMAL").get().getName());
                assertThat(user2.getMembershipGrade().getName()).isEqualTo(membershipGradeRepository.findByName("NORMAL").get().getName());
                assertThat(user3.getMembershipGrade().getName()).isEqualTo(membershipGradeRepository.findByName("VIP").get().getName());
                assertThat(user4.getMembershipGrade().getName()).isEqualTo(membershipGradeRepository.findByName("VVIP").get().getName());

                assertThat(pointSupportService.calculateUserPoints(user1.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(10));
                assertThat(pointSupportService.calculateUserPoints(user2.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(500));
                assertThat(pointSupportService.calculateUserPoints(user3.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(3000));
                assertThat(pointSupportService.calculateUserPoints(user4.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(7500));

                tx.commit();
            } finally {
                em.close();
            }
        }
    }

    @RepeatedTest(2)
    public void 포인트는_환불_기간_이전거는_포함되지_않는다() {
        LocalDateTime timeNow = LocalDateTime.now();

        // 8일 전으로 시간 여행
        LocalDateTime timeBeforeRefund = timeNow.minus(constants.getRefundPeriod()).minusMinutes(1);

        Mockito.when(clock.instant()).thenReturn(timeBeforeRefund.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // user 생성
        User user1;

        {
            MembershipGrade membership = membershipGradeRepository.findByName("NORMAL").get();
            EntityManager em = emf.createEntityManager();

            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                user1 = User.create(
                        membership,
                        "김희찬",
                        "user1@gmail.com",
                        passwordEncoder.encode("1234qwer"),
                        "01011112222"
                );

                em.persist(user1);
                em.flush();

                tx.commit();
            } finally {
                em.close();
            }
        }

        // 가짜 구매 내역 생성
        var orderPaymentOld = createFakePurchase(user1, BigDecimal.valueOf(1000), timeBeforeRefund);

        // 포인트 생성
        pointService.createPointAfterPaymentConfirm(
                orderPaymentOld.getSecond().getId(),
                orderPaymentOld.getFirst().getId(),
                user1.getId()
        );

        // 현재로 돌아옴
        Mockito.when(clock.instant()).thenReturn(timeNow.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // 포인트 계산
        pointTasks.calculateMembershipAndReadyPoints();

        // 포인트와 멤버쉽이 맞는지 확인
        {
            EntityManager em = emf.createEntityManager();
            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                user1 = em.find(User.class, Long.valueOf(user1.getId()));

                assertThat(user1.getMembershipGrade().getName()).isEqualTo(membershipGradeRepository.findByName("NORMAL").get().getName());
                assertThat(pointSupportService.calculateUserPoints(user1.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(10));

                tx.commit();
            } finally {
                em.close();
            }
        }

        // 현재 가짜 구매 내역 생성
        var orderPaymentNew = createFakePurchase(user1, BigDecimal.valueOf(150000), timeNow);

        // 포인트 생성
        pointService.createPointAfterPaymentConfirm(
                orderPaymentNew.getSecond().getId(),
                orderPaymentNew.getFirst().getId(),
                user1.getId()
        );

        LocalDateTime timeFuture = timeNow.plusNanos(constants.getRefundPeriod().toNanos() / 2);

        // 미래로 이동
        Mockito.when(clock.instant()).thenReturn(timeFuture.atZone(ZoneId.systemDefault()).toInstant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // 포인트 계산
        pointTasks.calculateMembershipAndReadyPoints();

        // 포인트와 멤버쉽이 맞는지 확인
        {
            EntityManager em = emf.createEntityManager();
            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                user1 = em.find(User.class, Long.valueOf(user1.getId()));

                assertThat(user1.getMembershipGrade().getName()).isEqualTo(membershipGradeRepository.findByName("NORMAL").get().getName());
                assertThat(pointSupportService.calculateUserPoints(user1.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(10));

                tx.commit();
            } finally {
                em.close();
            }
        }
    }

    private Pair<Order, Payment> createFakePurchase(
            User user,
            BigDecimal total,
            LocalDateTime paidAt
    ) {
        EntityManager em = emf.createEntityManager();

        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            // 주문 생성
            Order order = new Order(total, user);

            order.setStatusToCompleted();

            // 결제 생성
            Payment payment = Payment.createAttempt(
                    user.getId(),
                    order,
                    total,
                    BigDecimal.ZERO,
                    UUID.randomUUID().toString()
            );

            payment.confirm(
                    UUID.randomUUID().toString(),
                    total,
                    paidAt
            );

            em.persist(order);
            em.persist(payment);
            em.flush();

            tx.commit();

            return Pair.of(order, payment);
        }finally {
            em.close();
        }
    }
}
