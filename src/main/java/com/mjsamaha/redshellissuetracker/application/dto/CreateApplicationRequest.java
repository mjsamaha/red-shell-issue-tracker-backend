package com.mjsamaha.redshellissuetracker.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9][a-z0-9-]{0,62}$") String slug,
        @Size(max = 2000) String description,
        List<@Size(max = 2048) String> allowedOrigins,
        Boolean publicVisible,
        Boolean submissionsEnabled,
        Boolean requiresModeration) { }
