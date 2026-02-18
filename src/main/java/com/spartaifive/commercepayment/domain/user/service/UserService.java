package com.spartaifive.commercepayment.domain.user.service;

import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.common.security.JwtTokenProvider;
import com.spartaifive.commercepayment.domain.user.dto.request.LoginRequest;
import com.spartaifive.commercepayment.domain.user.dto.request.SignupRequest;
import com.spartaifive.commercepayment.domain.user.dto.response.PaymentUserResponse;
import com.spartaifive.commercepayment.domain.user.dto.response.SignupResponse;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.entity.UserRefreshToken;
import com.spartaifive.commercepayment.domain.user.repository.MembershipGradeRepository;
import com.spartaifive.commercepayment.domain.user.repository.UserRefreshTokenRepository;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.spartaifive.commercepayment.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MembershipGradeRepository membershipGradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입 기능
     * 정은식
     */
    public SignupResponse signup(SignupRequest request) {
        // 이메일, 전화번호 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ServiceErrorException(ERR_DUPLICATED_EMAIL);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ServiceErrorException(ERR_DUPLICATED_PHONE);
        }
        // 기본 멤버십 조회
        MembershipGrade normalGrade = membershipGradeRepository.findByName("NORMAL")
                .orElseThrow(() -> new ServiceErrorException(ERR_MEMBERSHIP_GRADE_NOT_FOUND));
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        // User 생성 (도메인 책임)
        User user = User.create(
                normalGrade,
                request.getName(),
                request.getEmail(),
                encodedPassword,
                request.getPhone()
        );
        // 저장
        User savedUser = userRepository.save(user);
        // Response DTO 반환
        return new SignupResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getMembershipUpdatedDate()
        );
    }

    /**
     * 로그인 기능
     * 정은식
     */
    @Transactional
    public String login(LoginRequest request) {

        // 이메일로 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ServiceErrorException(ERR_INVALID_CREDENTIALS)
                );
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ServiceErrorException(ERR_INVALID_CREDENTIALS);
        }

        // 기존 Refresh Token 정리 - 재로그인
        userRefreshTokenRepository.deleteByUser(user);

        // 리프레시 토큰 생성
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        // Refresh Token 생성 (정적 팩토리 메서드 사용)
        UserRefreshToken refreshToken = UserRefreshToken.create(
                user,
                refreshTokenValue,
                LocalDateTime.now().plusDays(7)
        );

        userRefreshTokenRepository.save(refreshToken);

        // AccessToken 생성 -> 반환
        return jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * 엑세스토큰 재발급 기능
     * 정은식
     */
    @Transactional
    public String refreshAccessToken(String refreshTokenValue) {

        // refresh token 조회
        UserRefreshToken refreshToken = userRefreshTokenRepository
                .findByRefreshToken(refreshTokenValue)
                .orElseThrow(() ->
                        new ServiceErrorException(ERR_REFRESH_TOKEN_INVALID)
                );

        // 로그아웃 여부 확인
        if (refreshToken.getRevokedAt() != null) {
            throw new ServiceErrorException(ERR_REFRESH_TOKEN_REVOKED);
        }

        // 만료 여부 확인
        if (refreshToken.getExpirationAt().isBefore(LocalDateTime.now())) {
            throw new ServiceErrorException(ERR_REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        // 새 Access Token 발급
        return jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * 로그아웃 기능
     * 정은식
     */
    @Transactional
    public void logout(String refreshTokenValue) {

        UserRefreshToken refreshToken = userRefreshTokenRepository
                .findByRefreshToken(refreshTokenValue)
                .orElseThrow(() ->
                        new ServiceErrorException(ERR_REFRESH_TOKEN_INVALID)
                );
        // 이미 로그아웃된 토큰이면 통과
        if (refreshToken.getRevokedAt() != null) {
            return;
        }
        // 로그아웃 처리
        refreshToken.revoke();
    }

    /**
     * 정보 조회 기능
     * 정은식
     * 결제를 위한?? 정보조회 PortOne에게 보내는 데이터
     */
    public PaymentUserResponse getPaymentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceErrorException(ERR_USER_NOT_FOUND));

        // AuthController 참고하여 키 만듬
        String customerUid = "CUST_" + user.getId();

        // 번호 변환 메서드
        String formattedPhone = formatPhoneNumber(user.getPhone());

        return new PaymentUserResponse(
                customerUid,
                user.getEmail(),
                user.getName(),
                formattedPhone,
                user.getPointsReadyToSpend(),
                user.getPointsNotReadyToSpend()
        );
    }

    // 핸드폰번호 변환 ex) 01000000000 -> 010-0000-0000
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }
}

