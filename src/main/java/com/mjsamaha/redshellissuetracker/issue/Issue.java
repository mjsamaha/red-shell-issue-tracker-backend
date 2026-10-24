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
    
    protected Issue() {
    }

    public Issue(Application application, long number, String title,
                 String description, IssueType type) {
        this.application = application;
        this.number = number;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public Application getApplication() {
        return application;
    }

    public long getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public ModerationState getModerationState() {
        return moderationState;
    }

    public void setModerationState(ModerationState moderationState) {
        this.moderationState = moderationState;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public TenantUser getAssignee() {
        return assignee;
    }

    public void setAssignee(TenantUser assignee) {
        this.assignee = assignee;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public Map<String, Object> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, Object> environment) {
        this.environment = environment;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public Issue getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Issue duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}