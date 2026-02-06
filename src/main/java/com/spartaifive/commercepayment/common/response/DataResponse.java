package com.spartaifive.commercepayment.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({ "success", "code", "data" })
public class DataResponse<T> extends BaseResponse {
    private T data;

    public static <T> DataResponse<T> success(String code, T data) {
        DataResponse<T> response = new DataResponse<>();

        response.success = true;
        response.code = code;
        response.data = data;

        return response;
    }

    public static <T> DataResponse<T> fail(String code, T data) {
        DataResponse<T> response = new DataResponse<>();

        response.success = false;
        response.code = code;
        response.data = data;

        return response;
    }
}
