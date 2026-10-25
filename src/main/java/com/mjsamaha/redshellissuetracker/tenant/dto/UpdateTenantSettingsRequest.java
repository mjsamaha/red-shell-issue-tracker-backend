package com.mjsamaha.redshellissuetracker.tenant.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTenantSettingsRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String logoUrl,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String primaryColor,
        Map<String, Object> theme,
        boolean moderationDefault,
        boolean requireReporterEmail) { }
