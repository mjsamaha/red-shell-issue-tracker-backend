package com.mjsamaha.redshellissuetracker.tenant.dto;

import java.util.Map;
import java.util.UUID;

import com.mjsamaha.redshellissuetracker.common.TenantStatus;
import com.mjsamaha.redshellissuetracker.tenant.Tenant;

public record TenantResponse(UUID id, String name, String slug, String logoUrl, String primaryColor,
                             Map<String, Object> theme, TenantStatus status, boolean moderationDefault,
                             boolean requireReporterEmail, int maxIssuesPerDay, int maxApplications,
                             long maxAttachmentBytes) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.getLogoUrl(),
                tenant.getPrimaryColor(), tenant.getThemeJson(), tenant.getStatus(), tenant.isModerationDefault(),
                tenant.isRequireReporterEmail(), tenant.getMaxIssuesPerDay(), tenant.getMaxApplications(),
                tenant.getMaxAttachmentBytes());
    }
}
