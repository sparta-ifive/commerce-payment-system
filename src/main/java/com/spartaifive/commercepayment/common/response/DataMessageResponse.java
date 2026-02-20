package com.spartaifive.commercepayment.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({ "success", "code", "message", "data" })
public class DataMessageResponse<T> extends BaseResponse {
    private String message;

    private T data;

    public static <T> DataMessageResponse<T> success(String code, String message, T data) {
        DataMessageResponse<T> response = new DataMessageResponse<>();

        response.success = true;
        response.code = code;
        response.message = message;
        response.data = data;

        return response;
    }

    public static <T> DataMessageResponse<T> fail(String code, String message, T data) {
        DataMessageResponse<T> response = new DataMessageResponse<>();

        response.success = false;
        response.code = code;
        response.message = message;
        response.data = data;

        return response;
    }
}
