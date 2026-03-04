package com.khoi.game_library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${steam.api.base-url}")
    private String steamBaseUrl;

    @Bean
    public RestClient steamRestClient() {
        return RestClient.builder()
                .baseUrl(steamBaseUrl)
                .build();
    }
}