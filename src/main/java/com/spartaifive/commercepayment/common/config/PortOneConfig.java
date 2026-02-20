package com.spartaifive.commercepayment.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@RequiredArgsConstructor
@Configuration

public class PortOneConfig {

    private final PortOneProperties properties;
    @Bean
    public RestClient portOneRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getApi().getBaseUrl())
                .defaultHeader("Authorization", "PortOne " + properties.getApi().getSecret())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(clientHttpRequestFactory())
                .build();
    }
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(60));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }
}
