package com.mjsamaha.redshellissuetracker.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

    public static final String TENANT_BY_SLUG = "tenantBySlug";

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(TENANT_BY_SLUG);
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(5)));
        return manager;
    }
}
