package com.khoi.game_library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Configuration
public class RestClientConfig {

    @Value("${steam.api.base-url}")
    private String steamBaseUrl;

    @Bean
    public RestClient steamRestClient() {
        String safeBaseUrl = Objects.requireNonNull(steamBaseUrl, "steam.api.base-url must not be null");
        return RestClient.builder()
            .baseUrl(safeBaseUrl)
                .build();
    }
}