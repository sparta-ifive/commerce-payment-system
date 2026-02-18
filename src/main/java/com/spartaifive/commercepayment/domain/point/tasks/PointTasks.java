package com.spartaifive.commercepayment.domain.point.tasks;

import com.spartaifive.commercepayment.common.constants.Constants;
import com.spartaifive.commercepayment.domain.point.service.PointSupportService;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.repository.MembershipGradeRepository;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointTasks {
    private final Clock clock;
    private final UserRepository userRepository;
    private final MembershipGradeRepository membershipGradeRepository;
    private final PointSupportService pointSupportService;
    private final Constants constants;

    private static final int BATCH_SIZE = 100;

    @Scheduled(cron = "${app.schedules.point-membership-batching}")
    @Transactional
    public void calculateMembershipAndReadyPoints() {
        LocalDateTime now = LocalDateTime.now(clock);

        log.info("[POINT_TASK]: started updating user points and memberships");

        // TODO: 현재 시각을 기준으로 refund 기간 이전 결제를 환불 가능 결제로 칭합니다.
        // 하지만 현재 시각의 기준이 이 컴퓨터를 돌리고 있는 timezone을 기준으로 하기 때문에 명확하지 않은듯 합니다.
        LocalDateTime paymentConfirmDay = now.minus(constants.getRefundPeriod());

        List<Long> userIds = userRepository.findAllUserId();
        List<MembershipGrade> memberships = membershipGradeRepository.findAll();
        memberships = memberships.stream().sorted((a, b) -> a.getRequiredPurchaseAmount().compareTo(b.getRequiredPurchaseAmount())).toList();

        // jpa paging을 사용할 수도 있지만...
        // 유저가 20억명 이하라면 괜찮을 거 같습니다.
        int userCursor = 0;

        while (userCursor < userIds.size()) {
            int begin = userCursor;
            int end = userCursor + BATCH_SIZE;

            if (end > userIds.size()) {
                end = userIds.size();
            }

            List<Long> subUsers = userIds.subList(begin, end);

            try {
                pointSupportService.updateUserMembership(
                        subUsers, memberships, paymentConfirmDay);
            }catch(Exception e) {
                log.error("[POINT_TASK]: failed to update users {}-{} membership : {}", begin, end, e.getMessage());
                continue; // 고객의 멤버쉽 등급이 결정되지 않으면 포인트를 계산 할 수 없으므로 skip
            }

            try {
                pointSupportService.updateUserPoints(
                        subUsers, memberships, paymentConfirmDay);
            }catch(Exception e) {
                log.error("[POINT_TASK]: failed to update users points {}-{} {}", begin, end, e.getMessage());

                continue; // 고객의 포인트가 업데이트 되지 않으면 포인트 총량을 계산 할 수 없으므로 skip
            }

            try {
                pointSupportService.updateUserPointsTotal(subUsers);
            } catch(Exception e) {
                log.error("[POINT_TASK]: failed to update users points total {}-{} {}", begin, end, e.getMessage());
            }

            userCursor += BATCH_SIZE;
        }

        log.info("[POINT_TASK]: finished updating user points and memberships");
    }
}
