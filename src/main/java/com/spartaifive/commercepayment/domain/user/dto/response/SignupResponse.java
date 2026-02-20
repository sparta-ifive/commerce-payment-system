package com.spartaifive.commercepayment.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long userId;
    private String userName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime membershipUpdatedDate;
}
