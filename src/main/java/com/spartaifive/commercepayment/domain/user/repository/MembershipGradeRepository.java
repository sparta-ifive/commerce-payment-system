package com.spartaifive.commercepayment.domain.user.repository;

import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipGradeRepository extends JpaRepository<MembershipGrade, Long> {

    Optional<MembershipGrade> findByName(String name);
}
