-- V1__init.sql
-- Red Shell Issue Tracker: initial schema.
-- Mirrors the JPA entities. Column names follow Spring Boot's default
-- snake_case naming (createdAt -> created_at, tenantId -> tenant_id).
-- Every table has the BaseEntity columns: id, created_at, updated_at, version.
-- Every table except tenants has tenant_id (TenantScopedEntity).

-- ---------------------------------------------------------------------------
-- tenants (not tenant-scoped)
-- ---------------------------------------------------------------------------
CREATE TABLE tenants (
    id                      uuid         PRIMARY KEY,
    created_at              timestamptz  NOT NULL,
    updated_at              timestamptz  NOT NULL,
    version                 bigint       NOT NULL,

    slug                    varchar(63)  NOT NULL UNIQUE,
    name                    varchar(100) NOT NULL,
    status                  varchar(20)  NOT NULL,
    logo_url                varchar(500),
    primary_color           varchar(7),
    theme_json              jsonb,
    moderation_default      boolean      NOT NULL,
    require_reporter_email  boolean      NOT NULL,
    max_issues_per_day      integer      NOT NULL,
    max_applications        integer      NOT NULL,
    max_attachment_bytes    bigint       NOT NULL
);

-- ---------------------------------------------------------------------------
-- tenant_users (staff)
-- ---------------------------------------------------------------------------
CREATE TABLE tenant_users (
    id             uuid         PRIMARY KEY,
    created_at     timestamptz  NOT NULL,
    updated_at     timestamptz  NOT NULL,
    version        bigint       NOT NULL,
    tenant_id      uuid         NOT NULL REFERENCES tenants (id),

    email          varchar(254) NOT NULL,
    password_hash  varchar(100) NOT NULL,
    display_name   varchar(100) NOT NULL,
    role           varchar(20)  NOT NULL,
    enabled        boolean      NOT NULL,
    last_login_at  timestamptz,

    CONSTRAINT uq_tenant_users_tenant_email UNIQUE (tenant_id, email)
);

-- ---------------------------------------------------------------------------
-- applications
-- ---------------------------------------------------------------------------
CREATE TABLE applications (
    id                    uuid          PRIMARY KEY,
    created_at            timestamptz   NOT NULL,
    updated_at            timestamptz   NOT NULL,
    version               bigint        NOT NULL,
    tenant_id             uuid          NOT NULL REFERENCES tenants (id),

    name                  varchar(100)  NOT NULL,
    slug                  varchar(63)   NOT NULL,
    description           varchar(2000),
    project_key           varchar(64)   NOT NULL UNIQUE,
    allowed_origins       jsonb,
    public_visible        boolean       NOT NULL,
    submissions_enabled   boolean       NOT NULL,
    requires_moderation   boolean       NOT NULL,
    archived_at           timestamptz,
    next_issue_number     bigint        NOT NULL,

    CONSTRAINT uq_applications_tenant_slug UNIQUE (tenant_id, slug)
);

-- ---------------------------------------------------------------------------
-- reporters
-- ---------------------------------------------------------------------------
CREATE TABLE reporters (
    id                         uuid         PRIMARY KEY,
    created_at                 timestamptz  NOT NULL,
    updated_at                 timestamptz  NOT NULL,
    version                    bigint       NOT NULL,
    tenant_id                  uuid         NOT NULL REFERENCES tenants (id),

    email                      varchar(254),
    email_verified             boolean      NOT NULL,
    display_name               varchar(100),
    verification_token_hash    varchar(64),
    verification_expires_at    timestamptz,

    CONSTRAINT uq_reporters_tenant_email UNIQUE (tenant_id, email)
);

-- ---------------------------------------------------------------------------
-- issues
-- ---------------------------------------------------------------------------
CREATE TABLE issues (
    id                uuid         PRIMARY KEY,
    created_at        timestamptz  NOT NULL,
    updated_at        timestamptz  NOT NULL,
    version           bigint       NOT NULL,
    tenant_id         uuid         NOT NULL REFERENCES tenants (id),

    application_id    uuid         NOT NULL REFERENCES applications (id),
    number            bigint       NOT NULL,
    title             varchar(200) NOT NULL,
    description       text         NOT NULL,
    type              varchar(20)  NOT NULL,
    severity          varchar(20),
    status            varchar(20)  NOT NULL,
    visibility        varchar(20)  NOT NULL,
    moderation_state  varchar(20)  NOT NULL,
    reporter_id       uuid         REFERENCES reporters (id),
    assignee_id       uuid         REFERENCES tenant_users (id),
    app_version       varchar(64),
    os                varchar(100),
    browser           varchar(100),
    environment       jsonb,
    vote_count        integer      NOT NULL,
    duplicate_of_id   uuid         REFERENCES issues (id),
    resolved_at       timestamptz,

    CONSTRAINT uq_issues_application_number UNIQUE (application_id, number)
);

CREATE INDEX idx_issues_list
    ON issues (tenant_id, application_id, status, created_at);

-- ---------------------------------------------------------------------------
-- comments
-- ---------------------------------------------------------------------------
CREATE TABLE comments (
    id              uuid          PRIMARY KEY,
    created_at      timestamptz   NOT NULL,
    updated_at      timestamptz   NOT NULL,
    version         bigint        NOT NULL,
    tenant_id       uuid          NOT NULL REFERENCES tenants (id),

    issue_id        uuid          NOT NULL REFERENCES issues (id),
    author_type     varchar(20)   NOT NULL,
    author_user_id  uuid          REFERENCES tenant_users (id),
    reporter_id     uuid          REFERENCES reporters (id),
    body            varchar(5000) NOT NULL,
    internal        boolean       NOT NULL
);

CREATE INDEX idx_comments_issue ON comments (issue_id);

-- ---------------------------------------------------------------------------
-- votes
-- ---------------------------------------------------------------------------
CREATE TABLE votes (
    id          uuid         PRIMARY KEY,
    created_at  timestamptz  NOT NULL,
    updated_at  timestamptz  NOT NULL,
    version     bigint       NOT NULL,
    tenant_id   uuid         NOT NULL REFERENCES tenants (id),

    issue_id    uuid         NOT NULL REFERENCES issues (id),
    voter_key   varchar(64)  NOT NULL,

    CONSTRAINT uq_votes_issue_voter UNIQUE (issue_id, voter_key)
);

-- ---------------------------------------------------------------------------
-- issue_status_history (append-only)
-- ---------------------------------------------------------------------------
CREATE TABLE issue_status_history (
    id                  uuid         PRIMARY KEY,
    created_at          timestamptz  NOT NULL,
    updated_at          timestamptz  NOT NULL,
    version             bigint       NOT NULL,
    tenant_id           uuid         NOT NULL REFERENCES tenants (id),

    issue_id            uuid         NOT NULL REFERENCES issues (id),
    from_status         varchar(20),
    to_status           varchar(20)  NOT NULL,
    changed_by_user_id  uuid         REFERENCES tenant_users (id)
);

CREATE INDEX idx_issue_status_history_issue ON issue_status_history (issue_id);

-- ---------------------------------------------------------------------------
-- attachments (optional in v1; harmless if the entity is not created yet)
-- ---------------------------------------------------------------------------
