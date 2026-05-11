package com.vitaltrip.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String refreshSecret,
        String expiresIn,
        String refreshExpiresIn
) {}
