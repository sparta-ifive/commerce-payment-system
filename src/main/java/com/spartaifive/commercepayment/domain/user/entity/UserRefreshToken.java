package com.spartaifive.commercepayment.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String refreshToken;

    // 생성일
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 만료일 7일
    @Column(nullable = false)
    private LocalDateTime expirationAt;

    // 로그아웃시 토큰 무효화
    private LocalDateTime revokedAt; // 로그아웃시 토큰 무효화


    // 생성일은 엔티티에 추가시 자동생성
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 로그아웃 시 토큰 무효화 처리
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    // Refresh Token 생성용 팩토리 메서드
    public static UserRefreshToken create(
            User user,
            String refreshToken,
            LocalDateTime expirationAt
    ) {
        UserRefreshToken token = new UserRefreshToken();
        token.user = user;
        token.refreshToken = refreshToken;
        token.expirationAt = expirationAt;
        return token;
    }
}