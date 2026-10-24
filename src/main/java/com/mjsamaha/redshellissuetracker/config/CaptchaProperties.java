package com.mjsamaha.redshellissuetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.captcha")
public record CaptchaProperties(String provider, String siteKey, String secretKey) {
}
