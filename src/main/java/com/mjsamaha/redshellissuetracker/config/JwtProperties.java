package com.mjsamaha.redshellissuetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String issuer, String audience, String secret) {
}
