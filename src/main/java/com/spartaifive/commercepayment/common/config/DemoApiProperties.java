package com.spartaifive.commercepayment.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.demo-api")
public class DemoApiProperties {
    private String baseUrl;
    private Map<String, String> endpoints;
}
