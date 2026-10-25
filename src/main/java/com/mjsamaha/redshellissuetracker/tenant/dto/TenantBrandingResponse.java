package com.mjsamaha.redshellissuetracker.tenant.dto;

import java.util.Map;

import com.mjsamaha.redshellissuetracker.tenant.Tenant;

public record TenantBrandingResponse(String name, String slug, String logoUrl, String primaryColor,
                                     Map<String, Object> theme) {
    public static TenantBrandingResponse from(Tenant tenant) {
        return new TenantBrandingResponse(tenant.getName(), tenant.getSlug(), tenant.getLogoUrl(),
                tenant.getPrimaryColor(), tenant.getThemeJson());
    }
}
