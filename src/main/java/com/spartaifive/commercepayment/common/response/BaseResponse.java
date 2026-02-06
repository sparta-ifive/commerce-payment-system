package com.spartaifive.commercepayment.common.response;

import lombok.Getter;

@Getter
public abstract class BaseResponse {
    protected boolean success;
    protected String code;
}
