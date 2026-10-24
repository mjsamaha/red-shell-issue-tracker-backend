package com.mjsamaha.redshellissuetracker.vote;

import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;
import com.mjsamaha.redshellissuetracker.issue.Issue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "votes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"issue_id", "voter_key"})
)
public class Vote extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false, updatable = false)
    private Issue issue;

    @Column(name = "voter_key", nullable = false, updatable = false, length = 64)
    private String voterKey;
    // Hash of the verified reporter ID, or of a signed cookie / IP + user agent
    // for anonymous voters. Never store a raw IP address.
}