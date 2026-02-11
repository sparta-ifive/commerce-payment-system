package com.spartaifive.commercepayment.common.initializer;

import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.repository.MembershipGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component()
@ConditionalOnProperty(
        name = "app.add-test-memberships",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class MemberShipInitializer implements ApplicationRunner {
    private final MembershipGradeRepository membershipGradeRepository;

    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        membershipGradeRepository.save(new MembershipGrade("NORMAL", 1L));
        membershipGradeRepository.save(new MembershipGrade("VIP", 3L));
        membershipGradeRepository.save(new MembershipGrade("VVIP", 5L));
    }
}
