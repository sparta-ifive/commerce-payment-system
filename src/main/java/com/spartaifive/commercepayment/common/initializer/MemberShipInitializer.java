package com.spartaifive.commercepayment.common.initializer;

import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.repository.MembershipGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component()
@ConditionalOnProperty(
        name = "app.add-test-memberships",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class MemberShipInitializer implements ApplicationRunner {
    private final MembershipGradeRepository membershipGradeRepository;

    @Value("${app.add-test-memberships.generous:false}")
    private boolean beGenerous;

    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (beGenerous) {
            membershipGradeRepository.save(new MembershipGrade("NORMAL", 20L, BigDecimal.valueOf(5000)));
            membershipGradeRepository.save(new MembershipGrade("VIP", 40L, BigDecimal.valueOf(10000)));
            membershipGradeRepository.save(new MembershipGrade("VVIP", 50L, BigDecimal.valueOf(15000)));
        }else {
            membershipGradeRepository.save(new MembershipGrade("NORMAL", 1L, BigDecimal.valueOf(50000)));
            membershipGradeRepository.save(new MembershipGrade("VIP", 3L, BigDecimal.valueOf(100000)));
            membershipGradeRepository.save(new MembershipGrade("VVIP", 5L, BigDecimal.valueOf(150000)));
        }
    }
}
