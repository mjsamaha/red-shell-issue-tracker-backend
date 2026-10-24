package com.mjsamaha.redshellissuetracker.user;

import java.time.Instant;

import com.mjsamaha.redshellissuetracker.common.StaffRole;
import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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