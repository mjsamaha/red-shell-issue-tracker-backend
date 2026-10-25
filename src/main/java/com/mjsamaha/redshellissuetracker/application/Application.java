package com.mjsamaha.redshellissuetracker.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "applications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "slug"})
)
public class Application extends TenantScopedEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]{0,62}$")
    @Column(nullable = false, length = 63)
    private String slug;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Column(nullable = false, unique = true, length = 64)
    private String projectKey; // public identifier used by the widget

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> allowedOrigins = new ArrayList<>(); // widget CORS allowlist

    @Column(nullable = false)
    private boolean publicVisible = true;

    @Column(nullable = false)
    private boolean submissionsEnabled = true;

    @Column(nullable = false)
    private boolean requiresModeration = true;

    private Instant archivedAt;

    @Column(nullable = false)
    private long nextIssueNumber = 1L;
    
    protected Application() {
    }

    public Application(String name, String slug, String projectKey) {
        this.name = name;
        this.slug = slug;
        this.projectKey = projectKey;
    }

    /**
     * Returns the next issue number and advances the counter.
     * Call only while holding the PESSIMISTIC_WRITE lock on this row.
     */
    public long allocateIssueNumber() {
        return nextIssueNumber++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void rotateProjectKey(String projectKey) { this.projectKey = projectKey; }

    public void updateDetails(String name, String description, List<String> allowedOrigins,
                              boolean publicVisible, boolean submissionsEnabled, boolean requiresModeration) {
        this.name = name;
        this.description = description;
        this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : new ArrayList<>(allowedOrigins);
        this.publicVisible = publicVisible;
        this.submissionsEnabled = submissionsEnabled;
        this.requiresModeration = requiresModeration;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public boolean isPublicVisible() {
        return publicVisible;
    }

    public void setPublicVisible(boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    public boolean isSubmissionsEnabled() {
        return submissionsEnabled;
    }

    public void setSubmissionsEnabled(boolean submissionsEnabled) {
        this.submissionsEnabled = submissionsEnabled;
    }

    public boolean isRequiresModeration() {
        return requiresModeration;
    }

    public void setRequiresModeration(boolean requiresModeration) {
        this.requiresModeration = requiresModeration;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public long getNextIssueNumber() {
        return nextIssueNumber;
    }
}
