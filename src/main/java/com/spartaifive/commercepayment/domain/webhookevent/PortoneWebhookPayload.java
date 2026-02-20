package com.spartaifive.commercepayment.domain.webhookevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortoneWebhookPayload {
    @JsonProperty("tx_id")
    private String txId;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("status")
    private String status;
}
