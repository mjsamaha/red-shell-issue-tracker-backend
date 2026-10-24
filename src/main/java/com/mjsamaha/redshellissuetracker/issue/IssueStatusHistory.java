package com.mjsamaha.redshellissuetracker.issue;

import com.mjsamaha.redshellissuetracker.common.IssueStatus;
import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;
import com.mjsamaha.redshellissuetracker.user.TenantUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "issue_status_history")
public class IssueStatusHistory extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false, updatable = false)
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, updatable = false)
    private IssueStatus fromStatus; // null for the initial status

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private IssueStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", updatable = false)
    private TenantUser changedBy; // null for system changes (for example auto-close)
}