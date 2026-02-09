package com.spartaifive.commercepayment.domain.user.controller;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.user.dto.request.LoginRequest;
import com.spartaifive.commercepayment.domain.user.dto.request.RefreshRequest;
import com.spartaifive.commercepayment.domain.user.dto.request.SignupRequest;
import com.spartaifive.commercepayment.domain.user.dto.response.SignupResponse;
import com.spartaifive.commercepayment.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
