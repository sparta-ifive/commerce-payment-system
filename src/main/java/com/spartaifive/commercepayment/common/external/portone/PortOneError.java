package com.spartaifive.commercepayment.common.external.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneError(
        String type,
        String message
) {
}
