package com.spartaifive.commercepayment.common.response;

import com.spartaifive.commercepayment.common.config.ClientApiProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicConfigResponse {
    private PortOneConfig portone;
    private ClientApiConfig api;
    private BrandingConfig branding;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortOneConfig {
        private String storeId;
        private Map<String, String> channelKeys;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientApiConfig {
        private String baseUrl;
        private Map<String, ClientApiProperties.EndpointContract> endpoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandingConfig {
        private String appName;
        private String tagline;
        private String logoText;
    }
}
