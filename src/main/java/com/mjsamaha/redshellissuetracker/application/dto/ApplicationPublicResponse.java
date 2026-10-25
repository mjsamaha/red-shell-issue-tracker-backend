package com.mjsamaha.redshellissuetracker.application.dto;

import com.mjsamaha.redshellissuetracker.application.Application;

public record ApplicationPublicResponse(String name, String slug, String description) {
    public static ApplicationPublicResponse from(Application app) {
        return new ApplicationPublicResponse(app.getName(), app.getSlug(), app.getDescription());
    }
}
