package com.vitaltrip.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleProperties(
        String clientId,
        String clientSecret,
        String callbackUrl,
        String placesApiKey
) {}
