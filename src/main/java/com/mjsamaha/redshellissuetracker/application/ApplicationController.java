package com.mjsamaha.redshellissuetracker.application;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mjsamaha.redshellissuetracker.application.dto.ApplicationPublicResponse;
import com.mjsamaha.redshellissuetracker.application.dto.ApplicationResponse;
import com.mjsamaha.redshellissuetracker.application.dto.CreateApplicationRequest;
import com.mjsamaha.redshellissuetracker.application.dto.UpdateApplicationRequest;

import jakarta.validation.Valid;

@RestController
public class ApplicationController {
    private final ApplicationService applications;

    public ApplicationController(ApplicationService applications) { this.applications = applications; }

    @GetMapping("/public/{tenantSlug}/applications")
    public List<ApplicationPublicResponse> publicApplications(@PathVariable String tenantSlug) {
        return applications.listPublic();
    }

    @GetMapping("/api/v1/applications")
    public List<ApplicationResponse> applications() { return applications.listStaff(); }

    @GetMapping("/api/v1/applications/{id}")
    public ApplicationResponse application(@PathVariable UUID id) { return applications.get(id); }

    @PostMapping("/api/v1/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody CreateApplicationRequest request) {
        return applications.create(request);
    }

    @PutMapping("/api/v1/applications/{id}")
    public ApplicationResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateApplicationRequest request) {
        return applications.update(id, request);
    }

    @PostMapping("/api/v1/applications/{id}/archive")
    public ApplicationResponse archive(@PathVariable UUID id) { return applications.archive(id); }

    @PostMapping("/api/v1/applications/{id}/unarchive")
    public ApplicationResponse unarchive(@PathVariable UUID id) { return applications.unarchive(id); }

    @PostMapping("/api/v1/applications/{id}/rotate-key")
    public ApplicationResponse rotateKey(@PathVariable UUID id) { return applications.rotateProjectKey(id); }
}
