package com.spartaifive.commercepayment.domain.user.controller;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.user.dto.request.LoginRequest;
import com.spartaifive.commercepayment.domain.user.dto.request.RefreshRequest;
import com.spartaifive.commercepayment.domain.user.dto.request.SignupRequest;
import com.spartaifive.commercepayment.domain.user.dto.response.PaymentUserResponse;
import com.spartaifive.commercepayment.domain.user.dto.response.SignupResponse;
import com.spartaifive.commercepayment.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     */
    @PostMapping("/signup")
    public ResponseEntity<DataResponse<SignupResponse>> signup(
            @RequestBody @Valid SignupRequest request
    ) {
        SignupResponse response = userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataResponse.success(String.valueOf(HttpStatus.CREATED.value()), response));
    }


    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<DataResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String accessToken = userService.login(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()),null));
    }

    /**
     * AccessToken 재발급 API
     */
    @PostMapping("/refresh")
    public ResponseEntity<DataResponse> refresh(
            @RequestBody @Valid RefreshRequest request
    ) {
        String newAccessToken = userService.refreshAccessToken(request.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), null));
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<DataResponse<Void>> logout(
            @RequestBody RefreshRequest request
    ) {
        userService.logout(request.getRefreshToken());

        return ResponseEntity.ok(DataResponse.success(String.valueOf(HttpStatus.OK.value()), null)
        );
    }


    @GetMapping("/me")
    public ResponseEntity<DataResponse<PaymentUserResponse>> getCurrentUser(
            Principal principal
    ) {
        // JWT 인증된 사용자 이메일
        String email = principal.getName();

        PaymentUserResponse response = userService.getPaymentUser(email);

        return ResponseEntity.ok(
                DataResponse.success(
                        String.valueOf(HttpStatus.OK.value()),
                        response
                )
        );
    }
}
