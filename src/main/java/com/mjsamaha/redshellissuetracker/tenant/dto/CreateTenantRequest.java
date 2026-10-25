package com.mjsamaha.redshellissuetracker.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$") String slug,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 254) String ownerEmail,
        @NotBlank @Size(max = 100) String ownerDisplayName) { }
