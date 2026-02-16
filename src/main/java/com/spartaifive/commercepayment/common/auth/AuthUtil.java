package com.spartaifive.commercepayment.common.auth;

import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.spartaifive.commercepayment.common.exception.ErrorCode.*;

/**
 * 인증 유틸리티
 * SecurityContext에서 현재 사용자 정보 추출
 */
public class AuthUtil {

    /**
     * 현재 인증된 사용자의 UserDetailsImpl 조회
     */
    private static UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceErrorException(ERR_TOKEN_MISSING);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }

        throw new ServiceErrorException(ERR_TOKEN_INVALID);
    }

    /**
     * 현재 인증된 사용자 ID 조회
     * (내 주문 / 내 결제 / 내 포인트 등 소유권 검증용)
     */
    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

    /**
     * 현재 인증된 사용자 이메일 조회
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserDetails().getEmail();
    }

    /**
     * 현재 인증된 사용자 이름 조회
     */
    public static String getCurrentUserName() {
        return getCurrentUserDetails().getName();
    }

    /**
     * 소유권 검증
     * 현재 사용자가 해당 리소스의 소유자인지 확인
     */
    public static void validateOwnership(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();

        if (!currentUserId.equals(resourceOwnerId)) {
            throw new ServiceErrorException(ERR_ACCESS_DENIED);
        }
    }
}
