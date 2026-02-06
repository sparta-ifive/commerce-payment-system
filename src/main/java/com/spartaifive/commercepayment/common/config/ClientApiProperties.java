package com.spartaifive.commercepayment.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 클라이언트 API 계약 설정
 * client-api-config.yml 파일의 내용을 바인딩합니다.
 */
@Data
@Component
@ConfigurationProperties(prefix = "api")
public class ClientApiProperties {
    private String baseUrl;
    private Map<String, EndpointContract> endpoints;

    @Data
    public static class EndpointContract {
        private String url;
        private String method;
        private String description;
        private List<PathParamDefinition> pathParams;
        private RequestSchema request;
        private ResponseSchema response;
    }

    @Data
    public static class RequestSchema {
        private List<FieldDefinition> fields;
    }

    @Data
    public static class ResponseSchema {
        private List<HeaderDefinition> headers;
        private BodySchema body;
    }

    @Data
    public static class BodySchema {
        private String type;  // "object" or "array"
        private List<FieldDefinition> fields;
        private List<FieldDefinition> items;  // for array type
    }

    @Data
    public static class FieldDefinition {
        private String name;
        private String type;
        private Boolean required;
        private String description;
        private String usage;
        private String example;
    }

    @Data
    public static class HeaderDefinition {
        private String name;
        private Boolean required;
        private String description;
    }

    @Data
    public static class PathParamDefinition {
        private String name;
        private String description;
    }
}
