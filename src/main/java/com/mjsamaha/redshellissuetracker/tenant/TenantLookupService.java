package com.mjsamaha.redshellissuetracker.tenant;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.mjsamaha.redshellissuetracker.config.CacheConfig.TENANT_BY_SLUG;

@Service
public class TenantLookupService {
    private final TenantRepository tenants;

    public TenantLookupService(TenantRepository tenants) {
        this.tenants = tenants;
    }

    @Cacheable(cacheNames = TENANT_BY_SLUG, key = "#slug")
    public Optional<Tenant> findBySlug(String slug) {
        return tenants.findBySlug(slug);
    }
}
