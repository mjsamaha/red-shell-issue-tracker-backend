# Red Shell Issue Tracker: Entity Model (Spring Boot / JPA)

Java field definitions for the core entities. Targets Spring Boot 3+ (Jakarta namespace), Hibernate 6.3+ (for `@TenantId`), and PostgreSQL. Flyway owns the schema; the annotations below mirror the migrations and should match them.

Accessors, constructors, and `equals`/`hashCode` are omitted for brevity. Add getters and setters with Lombok or by hand, whichever matches your other projects. Each entity needs a protected no-arg constructor. Base `equals`/`hashCode` on `id` only.

**Package layout (proposed):** `com.mjsamaha.redshellissuetracker`, split by feature (`tenant`, `user`, `application`, `issue`, `reporter`, `common`).

**Common imports used throughout:**

```java
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.*;
```

---

## 1. Base classes

Two superclasses: one for every entity, one adding the tenant discriminator.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;
}
```

```java
@MappedSuperclass
public abstract class TenantScopedEntity extends BaseEntity {

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;
}
```

**Notes**

- Enable auditing once with `@EnableJpaAuditing` on a `@Configuration` class.
- Never set `tenantId` by hand. Hibernate fills it from the `CurrentTenantIdentifierResolver` on insert and appends it to every query.
- The resolver's generic type must match the field type (`UUID` here).
- `Tenant` extends `BaseEntity` directly because it has no tenant of its own. Every other entity extends `TenantScopedEntity`.
- Register the resolver through a `HibernatePropertiesCustomizer` bean that puts it under `AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER`.

---

## 2. Enums

All persisted with `@Enumerated(EnumType.STRING)`, never `ORDINAL`.

```java
public enum TenantStatus    { ACTIVE, SUSPENDED }
public enum StaffRole       { OWNER, ADMIN, TRIAGER }
public enum IssueType       { BUG, FEATURE_REQUEST, FEEDBACK }
public enum Severity        { LOW, MEDIUM, HIGH, CRITICAL }
public enum IssueStatus     { OPEN, CONFIRMED, IN_PROGRESS, RESOLVED, CLOSED, WONT_FIX, DUPLICATE }
public enum ModerationState { PENDING, APPROVED, REJECTED, SPAM }
public enum Visibility      { PUBLIC, INTERNAL }
public enum AuthorType      { STAFF, REPORTER, ANONYMOUS }
public enum ScanStatus      { PENDING, CLEAN, INFECTED }
```

---

## 3. Tenant

Not tenant-scoped.

```java
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    @NotBlank
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$")
    @Column(nullable = false, unique = true, length = 63)
    private String slug;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status = TenantStatus.ACTIVE;

    // Branding
    @Size(max = 500)
    @Column(length = 500)
    private String logoUrl;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    @Column(length = 7)
    private String primaryColor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> themeJson;

    // Settings
    @Column(nullable = false)
    private boolean moderationDefault = true;

    @Column(nullable = false)
    private boolean requireReporterEmail = false;

    // Limits (starting defaults, tune per plan)
    @Column(nullable = false)
    private int maxIssuesPerDay = 200;

    @Column(nullable = false)
    private int maxApplications = 5;

    @Column(nullable = false)
    private long maxAttachmentBytes = 5_242_880L; // 5 MB
}
```

**Notes**

- Validate `slug` against a reserved-word list (`admin`, `api`, `public`, `t`, `www`) in the service layer.
- `slug` is globally unique because it appears in public URLs.

---

## 4. TenantUser (staff)

```java
@Entity
@Table(
    name = "tenant_users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"})
)
public class TenantUser extends TenantScopedEntity {

    @NotBlank
    @Email
    @Size(max = 254)
    @Column(nullable = false, length = 254)
    private String email; // store lowercased

    @NotBlank
    @Column(nullable = false, length = 100)
    private String passwordHash; // BCrypt or Argon2, never exposed in any DTO

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    private Instant lastLoginAt;
}
```

**Notes**

- Staff live inside a single tenant. If one person must manage several tenants later, split this into a global `User` plus a `Membership(user, tenant, role)` entity.

---

## 5. Application

```java
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
```

**Notes**

- `projectKey` is unique globally, not per tenant, because a widget request arrives with only the key. Generate it with `SecureRandom` (for example 24 random bytes, URL-safe Base64).
- Because `@TenantId` filters every query, resolving a tenant from a project key needs a small lookup that bypasses the filter, such as a native query or `JdbcClient` call returning the tenant ID. Run it before the tenant context is set.
- The field is named `publicVisible` rather than `isPublic` to avoid the `boolean isX` getter-naming problem (the bean property becomes `public`, not `isPublic`).
- Issue numbering: increment `nextIssueNumber` under a lock. A repository method annotated with `@Lock(LockModeType.PESSIMISTIC_WRITE)` that loads the application row works fine at this scale.

---

## 6. Reporter

Created only when someone gives an email address. Fully anonymous submissions have no `Reporter`.

```java
@Entity
@Table(
    name = "reporters",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"})
)
public class Reporter extends TenantScopedEntity {

    @Email
    @Size(max = 254)
    @Column(length = 254)
    private String email; // store lowercased

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Size(max = 100)
    @Column(length = 100)
    private String displayName;

    @Column(length = 64)
    private String verificationTokenHash; // store a hash, never the raw token

    private Instant verificationExpiresAt;
}
```

---

## 7. Issue

```java
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
```

**Notes**

- Publicly visible means `visibility == PUBLIC && moderationState == APPROVED`. Put that rule in one place, such as a repository specification or a derived query method.
- `number` is assigned in the same transaction as the insert, never derived from `MAX(number) + 1`.
- Cap the serialized size of `environment` in the service layer so a client cannot store arbitrary large JSON.
- Always map to DTOs. Never return this entity from a public endpoint.

---

## 8. Comment

```java
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
```

---

## 9. Vote

```java
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
```

**Notes**

- Insert the vote and increment `Issue.voteCount` in one transaction. A unique-constraint violation means the voter already voted.

---

## 10. IssueStatusHistory

Append-only audit trail. It also powers the public status timeline.

```java
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
```

---

## 11. Attachment (optional in v1)

Skip this until you decide where files are stored.

```java
@Entity
@Table(name = "attachments")
public class Attachment extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false, updatable = false)
    private Issue issue;

    @Column(nullable = false, updatable = false, length = 255)
    private String storageKey;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, length = 100)
    private String contentType; // validate against an allowlist

    @Column(nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus scanStatus = ScanStatus.PENDING;
}
```

---

## 12. Relationship summary

```
Tenant 1 ─── * Application 1 ─── * Issue 1 ─── * Comment
   │                                  │  ├─── * Vote
   │                                  │  ├─── * IssueStatusHistory
   │                                  │  ├─── * Attachment
   │                                  │  └─── 0..1 duplicateOf (Issue)
   ├─── * TenantUser ◄── Issue.assignee, Comment.authorUser
   └─── * Reporter   ◄── Issue.reporter, Comment.reporter
```

## 13. Conventions to keep consistent

- All associations are `FetchType.LAZY`. Fetch what a screen needs with explicit queries or entity graphs.
- Use `Instant` for every timestamp and store in UTC.
- Use `@Enumerated(EnumType.STRING)` everywhere.
- Set `spring.jpa.hibernate.ddl-auto=validate` so Flyway is the only schema authority.
- Composite unique constraints include `tenant_id` where uniqueness is per tenant. The exceptions are `Tenant.slug` and `Application.projectKey`, which are global.
- Public endpoints return DTOs only. They must never include `reporter`, `assignee`, `moderationState`, internal comments, or any email.