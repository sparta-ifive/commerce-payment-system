package com.spartaifive.commercepayment.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * 개선할 부분: Refresh Token, Token Expiry 관리, Claims 커스터마이징 등
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    // access token: 30분
    private final long accessTokenValidityMs = 30 * 60 * 1000L;

    // refresh token: 7일
    private final long refreshTokenValidityMs = 7 * 24 * 60 * 60 * 1000L;

    public JwtTokenProvider(
        @Value("${jwt.secret:commercehub-secret-key-for-demo-please-change-this-in-production-environment}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access TOken 생성
     */
    public String createAccessToken(
            Long userId,
            String userName,
            String email
    ) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityMs);

        return Jwts.builder()
            .subject(email)
            .claim("userName", userName)
            .claim("userId", userId)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(
            Long userId,
            String userName,
            String email
    ) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMs);

        return Jwts.builder()
                .subject(email)
                .claim("userName", userName)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 토큰에서 사용자 이메일 추출
     */
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     */
    public String getName(String token) {
        return getClaims(token).get("userName", String.class);
    }

    /**
     * JWT 토큰에서 user id 추출
     */
    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    /**
     * JWT 토큰 유효성 검증
     *
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
