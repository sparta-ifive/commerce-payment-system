package com.spartaifive.commercepayment.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({ "success", "code", "message" })
public class MessageResponse<T> extends BaseResponse {
    private boolean success;
    private String code;
    private String message;

    public static <T> MessageResponse<T> success(String code, String message) {
        MessageResponse<T> response = new MessageResponse<>();

        response.success = true;
        response.code = code;
        response.message = message;

        return response;
    }

    public static <T> MessageResponse<T> fail(String code, String message) {
        MessageResponse<T> response = new MessageResponse<>();

        response.success = false;
        response.code = code;
        response.message = message;

        return response;
    }
}

