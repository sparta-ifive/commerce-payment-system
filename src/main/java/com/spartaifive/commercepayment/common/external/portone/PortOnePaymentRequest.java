package com.spartaifive.commercepayment.common.external.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentRequest(
        String channelKey,
        String orderName,
        Amount amount,
        String currency,
        PaymentMethod method
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(
            BigDecimal total
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentMethod(
            Card card
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
            Credential credential
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Credential(
            String number,
            String expiryYear,
            String expiryMonth,
            String birthOrBusinessRegistrationNumber,
            String passwordTwoDigits
    ) {
    }

    public static PortOnePaymentRequest of(String channelKey, String orderName, BigDecimal totalAmount, String currency,
                                           String cardNumber, String expiryYear, String expiryMonth,
                                           String birthOrBusinessRegistrationNumber, String passwordTwoDigits) {
        return new PortOnePaymentRequest(
                channelKey,
                orderName,
                new Amount(totalAmount),
                currency,
                new PaymentMethod(
                        new Card(
                                new Credential(
                                        cardNumber,
                                        expiryYear,
                                        expiryMonth,
                                        birthOrBusinessRegistrationNumber,
                                        passwordTwoDigits
                                )
                        )
                )
        );
    }
}
