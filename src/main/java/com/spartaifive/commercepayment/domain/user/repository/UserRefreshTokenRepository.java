package com.spartaifive.commercepayment.domain.user.repository;

import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.entity.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Refresh Token 관리용 Repository
 *
 * 로그인 시 Refresh Token 저장
 * Access Token 만료 시 Refresh Token 검증
 * 로그아웃,재로그인 시 기존 Refresh Token 무효화
 */
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    // Refresh Token 조회
    Optional<UserRefreshToken> findByRefreshToken(String refreshToken);

    // 특정 유저 Refresh Token 삭제
    void deleteByUser(User user);
}
