package com.mjsamaha.redshellissuetracker.application;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mjsamaha.redshellissuetracker.application.dto.ApplicationPublicResponse;
import com.mjsamaha.redshellissuetracker.application.dto.ApplicationResponse;
import com.mjsamaha.redshellissuetracker.application.dto.CreateApplicationRequest;
import com.mjsamaha.redshellissuetracker.application.dto.UpdateApplicationRequest;
import com.mjsamaha.redshellissuetracker.common.errors.ConflictException;
import com.mjsamaha.redshellissuetracker.common.errors.NotFoundException;
import com.mjsamaha.redshellissuetracker.tenant.TenantService;
import com.mjsamaha.redshellissuetracker.tenant.TenantContext;
import com.mjsamaha.redshellissuetracker.tenant.TenantRepository;

@Service
public class ApplicationService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApplicationRepository applications;
    private final TenantService tenants;
    private final TenantRepository tenantRepository;

    public ApplicationService(ApplicationRepository applications, TenantService tenants,
                              TenantRepository tenantRepository) {
        this.applications = applications;
        this.tenants = tenants;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request) {
        tenants.assertApplicationQuotaAvailable();
        if (applications.existsBySlug(request.slug())) throw new ConflictException("Application slug is already in use");
        boolean moderation = request.requiresModeration() != null ? request.requiresModeration()
                : tenantRepository.findById(TenantContext.get()).map(t -> t.isModerationDefault()).orElse(true);
        Application app = new Application(request.name().trim(), request.slug(), newProjectKey());
        app.updateDetails(request.name().trim(), request.description(), request.allowedOrigins(),
                request.publicVisible() == null || request.publicVisible(),
                request.submissionsEnabled() == null || request.submissionsEnabled(), moderation);
        try {
            return ApplicationResponse.from(applications.saveAndFlush(app));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Application slug or project key is already in use");
        }
    }

    @Transactional
    public ApplicationResponse update(UUID id, UpdateApplicationRequest request) {
        Application app = findCurrent(id);
        app.updateDetails(request.name().trim(), request.description(), request.allowedOrigins(),
                request.publicVisible(), request.submissionsEnabled(), request.requiresModeration());
        return ApplicationResponse.from(applications.save(app));
    }

    @Transactional
    public ApplicationResponse archive(UUID id) {
        Application app = findCurrent(id);
        app.setArchivedAt(Instant.now());
        return ApplicationResponse.from(applications.save(app));
    }

    @Transactional
    public ApplicationResponse unarchive(UUID id) {
        Application app = findCurrent(id);
        if (app.getArchivedAt() != null) tenants.assertApplicationQuotaAvailable();
        app.setArchivedAt(null);
        return ApplicationResponse.from(applications.save(app));
    }

    @Transactional
    public ApplicationResponse rotateProjectKey(UUID id) {
        Application app = findCurrent(id);
        app.rotateProjectKey(newProjectKey());
        return ApplicationResponse.from(applications.saveAndFlush(app));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(UUID id) {
        return ApplicationResponse.from(findCurrent(id));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listStaff() {
        return applications.findAll().stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationPublicResponse> listPublic() {
        return applications.findByPublicVisibleTrueAndArchivedAtIsNull().stream()
                .map(ApplicationPublicResponse::from).toList();
    }

    @Transactional
    public long allocateIssueNumber(UUID id) {
        Application app = applications.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return app.allocateIssueNumber();
    }

    private Application findCurrent(UUID id) {
        return applications.findById(id).orElseThrow(() -> new NotFoundException("Application not found"));
    }

    private static String newProjectKey() {
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }
}
