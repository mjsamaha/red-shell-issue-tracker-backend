package com.mjsamaha.redshellissuetracker.comment;

import com.mjsamaha.redshellissuetracker.common.AuthorType;
import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;
import com.mjsamaha.redshellissuetracker.issue.Issue;
import com.mjsamaha.redshellissuetracker.reporter.Reporter;
import com.mjsamaha.redshellissuetracker.user.TenantUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "comments")
public class Comment extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false, updatable = false)
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthorType authorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private TenantUser authorUser; // set when authorType == STAFF

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Reporter reporter; // set when authorType == REPORTER

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String body;

    @Column(nullable = false)
    private boolean internal = false; // internal comments never appear in public DTOs
}