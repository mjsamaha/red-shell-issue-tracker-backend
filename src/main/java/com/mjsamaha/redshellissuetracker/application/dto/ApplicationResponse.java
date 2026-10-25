package com.mjsamaha.redshellissuetracker.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mjsamaha.redshellissuetracker.application.Application;

public record ApplicationResponse(UUID id, String name, String slug, String description, String projectKey,
                                 List<String> allowedOrigins, boolean publicVisible, boolean submissionsEnabled,
                                 boolean requiresModeration, Instant archivedAt, long nextIssueNumber) {
    public static ApplicationResponse from(Application app) {
        return new ApplicationResponse(app.getId(), app.getName(), app.getSlug(), app.getDescription(),
                app.getProjectKey(), app.getAllowedOrigins(), app.isPublicVisible(), app.isSubmissionsEnabled(),
                app.isRequiresModeration(), app.getArchivedAt(), app.getNextIssueNumber());
    }
}
