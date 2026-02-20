package com.spartaifive.commercepayment.common.external.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneCancelRequest(
        String reason,
        BigDecimal amount
) {

    public static PortOneCancelRequest fullCancel(String reason) {
        return new PortOneCancelRequest(reason, null);
    }

    public static PortOneCancelRequest partialCancel(String reason, BigDecimal amount) {
        return new PortOneCancelRequest(reason, amount);
    }
}
