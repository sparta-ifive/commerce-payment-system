package com.spartaifive.commercepayment.domain.point.service;

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
import org.springframework.jdbc.core.JdbcTemplate;
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
    private PointTasks pointTasks;

    @Autowired
    DatabaseCleaner dbCleaner;

    @Autowired
    JdbcTemplate jdbcTemplate;

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
    public void test() {
        // ===================
        // GIVEN
        // ===================

        // 8일 전으로 시간 여행
        LocalDateTime time8DaysAgo = LocalDateTime.now().minusDays(8);

        Mockito.when(clock.instant()).thenReturn(time8DaysAgo.atZone(ZoneId.systemDefault()).toInstant());
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
        var orderPayment1 = createFakePurchase(user1, BigDecimal.valueOf(1000), time8DaysAgo);
        var orderPayment2 = createFakePurchase(user2, BigDecimal.valueOf(50000), time8DaysAgo);
        var orderPayment3 = createFakePurchase(user3, BigDecimal.valueOf(100000), time8DaysAgo);
        var orderPayment4 = createFakePurchase(user4, BigDecimal.valueOf(150000), time8DaysAgo);

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
        Mockito.when(clock.instant()).thenReturn(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
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

                assertThat(pointService.getUserPoints(user1.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(10));
                assertThat(pointService.getUserPoints(user2.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(500));
                assertThat(pointService.getUserPoints(user3.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(3000));
                assertThat(pointService.getUserPoints(user4.getId(), true)).isEqualByComparingTo(BigDecimal.valueOf(7500));

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
