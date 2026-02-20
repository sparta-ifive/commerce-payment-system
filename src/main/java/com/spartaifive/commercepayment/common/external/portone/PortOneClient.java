package com.spartaifive.commercepayment.common.external.portone;

import com.spartaifive.commercepayment.common.exception.PortOneApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class PortOneClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PortOneClient(RestClient portOneRestClient, ObjectMapper objectMapper) {
        this.restClient = portOneRestClient;
        this.objectMapper = objectMapper;
    }

    public PortOnePaymentResponse createInstantPayment(String paymentId, PortOnePaymentRequest request) {
        return restClient.post()
                .uri("/payments/{paymentId}/instant", paymentId)
                .header("Idempotency-Key", paymentId)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    PortOneError error = parseErrorResponse(res);
                    throw new PortOneApiException(
                            error != null ? error.type() : "UNKNOWN_ERROR",
                            error != null ? error.message() : "Unknown error occurred",
                            res.getStatusCode().value()
                    );
                })
                .body(PortOnePaymentResponse.class);
    }

    public PortOnePaymentResponse getPayment(String paymentId) {
        return restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    PortOneError error = parseErrorResponse(res);
                    throw new PortOneApiException(
                            error != null ? error.type() : "UNKNOWN_ERROR",
                            error != null ? error.message() : "Unknown error occurred",
                            res.getStatusCode().value()
                    );
                })
                .body(PortOnePaymentResponse.class);
    }

    public PortOneCancelResponse cancelPayment(String paymentId, PortOneCancelRequest request) {
        return restClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    PortOneError error = parseErrorResponse(res);
                    throw new PortOneApiException(
                            error != null ? error.type() : "UNKNOWN_ERROR",
                            error != null ? error.message() : "Unknown error occurred",
                            res.getStatusCode().value()
                    );
                })
                .body(PortOneCancelResponse.class);
    }

    private PortOneError parseErrorResponse(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return objectMapper.readValue(response.getBody(), PortOneError.class);
        } catch (Exception e) {
            return null;
        }
    }
}

