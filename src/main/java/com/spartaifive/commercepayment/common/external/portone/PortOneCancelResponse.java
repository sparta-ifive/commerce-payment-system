package com.spartaifive.commercepayment.common.external.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneCancelResponse(
        Cancellation cancellation
) {
    public boolean isSucceeded() {
        return cancellation != null
                && cancellation.status() != null
                && "SUCCEEDED".equalsIgnoreCase(cancellation.status().trim());
    }

    public BigDecimal cancelledTotalAmount() {
        return cancellation != null ? cancellation.totalAmount() : null;
    }

    public String cancelledAt() {
        return cancellation != null ? cancellation.cancelledAt() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancellation(
            String status,
            String id,
            String pgCancellationId,
            BigDecimal totalAmount,
            BigDecimal taxFreeAmount,
            BigDecimal vatAmount,
            String reason,
            String cancelledAt,
            String requestedAt,
            String receiptUrl,
            String trigger
    ) {}
}
