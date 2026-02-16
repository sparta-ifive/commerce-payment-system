package com.spartaifive.commercepayment.domain.user.repository;

import com.spartaifive.commercepayment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인용 이메일 찾기
    Optional<User> findByEmail(String email);

    // 회원가입용 중복 체크
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);


    @Query("SELECT u.id FROM User u")
    List<Long> findAllUserId();
}
