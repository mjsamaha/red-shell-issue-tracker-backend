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
}