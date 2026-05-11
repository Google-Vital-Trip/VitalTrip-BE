package com.vitaltrip.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        String nodeEnv,
        Cors cors
) {
    public record Cors(String allowedOrigin) {}
}
