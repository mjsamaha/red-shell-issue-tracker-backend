package com.mjsamaha.redshellissuetracker.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateApplicationRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 2000) String description,
        List<@Size(max = 2048) String> allowedOrigins,
        boolean publicVisible,
        boolean submissionsEnabled,
        boolean requiresModeration) { }
