package com.mjsamaha.redshellissuetracker.tenant;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mjsamaha.redshellissuetracker.common.StaffRole;
import com.mjsamaha.redshellissuetracker.common.errors.ConflictException;
import com.mjsamaha.redshellissuetracker.common.errors.NotFoundException;
import com.mjsamaha.redshellissuetracker.common.errors.QuotaExceededException;
import com.mjsamaha.redshellissuetracker.common.errors.InvalidRequestException;
import com.mjsamaha.redshellissuetracker.issue.IssueRepository;
import com.mjsamaha.redshellissuetracker.application.ApplicationRepository;
import com.mjsamaha.redshellissuetracker.tenant.dto.CreateTenantRequest;
import com.mjsamaha.redshellissuetracker.tenant.dto.TenantBrandingResponse;
import com.mjsamaha.redshellissuetracker.tenant.dto.TenantResponse;
import com.mjsamaha.redshellissuetracker.tenant.dto.UpdateTenantSettingsRequest;

import static com.mjsamaha.redshellissuetracker.config.CacheConfig.TENANT_BY_SLUG;

@Service
public class TenantService {
    private static final Set<String> RESERVED_SLUGS = Set.of("admin", "api", "public");
    private static final java.util.regex.Pattern SLUG_PATTERN =
            java.util.regex.Pattern.compile("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final TenantRepository tenants;
    private final TenantLookupService lookup;
    private final JdbcClient jdbc;
    private final ApplicationRepository applications;
    private final IssueRepository issues;

    public TenantService(TenantRepository tenants, TenantLookupService lookup, JdbcClient jdbc,
                         ApplicationRepository applications, IssueRepository issues) {
        this.tenants = tenants;
        this.lookup = lookup;
        this.jdbc = jdbc;
        this.applications = applications;
        this.issues = issues;
    }

    @Transactional
    public TenantResponse create(CreateTenantRequest request) {
        if (!SLUG_PATTERN.matcher(request.slug()).matches()) {
            throw new InvalidRequestException("Tenant slug must be 3 to 63 lowercase letters, numbers, or hyphens");
        }
        if (RESERVED_SLUGS.contains(request.slug())) throw new ConflictException("Tenant slug is reserved");
        if (tenants.existsBySlug(request.slug())) throw new ConflictException("Tenant slug is already in use");
        Tenant tenant = new Tenant(request.slug(), request.name());
        try {
            tenant = tenants.saveAndFlush(tenant);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Tenant slug is already in use");
        }

        // Phase 2 invitation flow replaces this unknowable password before enabling the owner.
        byte[] randomSecret = new byte[32];
        RANDOM.nextBytes(randomSecret);
        String pendingHash = PASSWORD_ENCODER.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(randomSecret));
        UUID userId = UUID.randomUUID();
        jdbc.sql("""
                insert into tenant_users (id, created_at, updated_at, version, tenant_id, email,
                    password_hash, display_name, role, enabled)
                values (:id, now(), now(), 0, :tenantId, :email, :passwordHash, :displayName, :role, false)
                """)
                .param("id", userId)
                .param("tenantId", tenant.getId())
                .param("email", request.ownerEmail().trim().toLowerCase(java.util.Locale.ROOT))
                .param("passwordHash", pendingHash)
                .param("displayName", request.ownerDisplayName().trim())
                .param("role", StaffRole.OWNER.name())
                .update();
        return TenantResponse.from(tenant);
    }

    @Transactional(readOnly = true)
    public TenantBrandingResponse getBranding(String slug) {
        Tenant tenant = lookup.findBySlug(slug).filter(t -> t.getStatus() == com.mjsamaha.redshellissuetracker.common.TenantStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
        return TenantBrandingResponse.from(tenant);
    }

    @Transactional(readOnly = true)
    public TenantResponse getCurrent() {
        return TenantResponse.from(currentTenant());
    }

    @Transactional
    @CacheEvict(cacheNames = TENANT_BY_SLUG, key = "#result.slug")
    public TenantResponse updateSettings(UpdateTenantSettingsRequest request) {
        Tenant tenant = currentTenant();
        tenant.updateSettings(request.name(), request.logoUrl(), request.primaryColor(), request.theme(),
                request.moderationDefault(), request.requireReporterEmail());
        return TenantResponse.from(tenants.save(tenant));
    }

    @Transactional
    @CacheEvict(cacheNames = TENANT_BY_SLUG, key = "#result.slug")
    public TenantResponse suspend(UUID id) {
        Tenant tenant = tenants.findById(id).orElseThrow(() -> new NotFoundException("Tenant not found"));
        tenant.suspend();
        return TenantResponse.from(tenants.save(tenant));
    }

    @Transactional
    @CacheEvict(cacheNames = TENANT_BY_SLUG, key = "#result.slug")
    public TenantResponse reactivate(UUID id) {
        Tenant tenant = tenants.findById(id).orElseThrow(() -> new NotFoundException("Tenant not found"));
        tenant.reactivate();
        return TenantResponse.from(tenants.save(tenant));
    }

    @Transactional(readOnly = true)
    public void assertApplicationQuotaAvailable() {
        Tenant tenant = currentTenant();
        if (applications.countByArchivedAtIsNull() >= tenant.getMaxApplications()) {
            throw new QuotaExceededException("Application limit reached for this tenant");
        }
    }

    @Transactional(readOnly = true)
    public void assertDailyIssueQuotaAvailable() {
        Tenant tenant = currentTenant();
        var start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        if (issues.countByCreatedAtGreaterThanEqual(start) >= tenant.getMaxIssuesPerDay()) {
            throw new QuotaExceededException("Daily issue limit reached for this tenant");
        }
    }

    private Tenant currentTenant() {
        UUID tenantId = TenantContext.get();
        if (TenantContext.NO_TENANT.equals(tenantId)) throw new NotFoundException("Tenant context is not available");
        return tenants.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant not found"));
    }
}
