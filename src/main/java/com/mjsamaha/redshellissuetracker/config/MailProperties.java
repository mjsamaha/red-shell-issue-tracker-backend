package com.mjsamaha.redshellissuetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.mail")
public record MailProperties(String fromAddress, String publicBaseUrl) {
}
