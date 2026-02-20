package com.spartaifive.commercepayment.common.exception;

import lombok.Getter;

@Getter
public class ServiceDataErrorException extends ServiceErrorException {
    // 참 슬프게도 Throwable을 상속 받은 class는 generic이 될 수 없기 때문에 Object 이어야 합니다
    private final Object data;

    public ServiceDataErrorException(ErrorCode errorCode, Object data) {
        super(errorCode);
        this.data = data;
    }

    public ServiceDataErrorException(ErrorCode errorCode, String overrideMessage, Object data) {
        super(errorCode, overrideMessage);
        this.data = data;
    }

    public Class<?> getDataClass() {
        return this.data.getClass();
    }
}
