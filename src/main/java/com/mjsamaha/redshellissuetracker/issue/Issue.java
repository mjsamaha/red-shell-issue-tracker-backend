package com.mjsamaha.redshellissuetracker.issue;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.mjsamaha.redshellissuetracker.application.Application;
import com.mjsamaha.redshellissuetracker.common.IssueStatus;
import com.mjsamaha.redshellissuetracker.common.IssueType;
import com.mjsamaha.redshellissuetracker.common.ModerationState;
import com.mjsamaha.redshellissuetracker.common.Severity;
import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;
import com.mjsamaha.redshellissuetracker.common.Visibility;
import com.mjsamaha.redshellissuetracker.reporter.Reporter;
import com.mjsamaha.redshellissuetracker.user.TenantUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "issues",
    uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "number"}),
    indexes = @Index(
        name = "idx_issues_list",
        columnList = "tenant_id, application_id, status, created_at"
    )
)
public class Issue extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, updatable = false)
    private Application application;

    @Column(nullable = false, updatable = false)
    private long number; // sequential per application, shown as #42

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 10_000)
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity; // null until staff triage; reporters cannot set it

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationState moderationState = ModerationState.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Reporter reporter; // null for fully anonymous reports

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private TenantUser assignee;

    // Client metadata
    @Size(max = 64)
    @Column(length = 64)
    private String appVersion;

    @Size(max = 100)
    @Column(length = 100)
    private String os;

    @Size(max = 100)
    @Column(length = 100)
    private String browser;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> environment; // anything else the client sends, size-capped

    @Column(nullable = false)
    private int voteCount = 0; // denormalized for sorting

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_id")
    private Issue duplicateOf;

    private Instant resolvedAt;
}