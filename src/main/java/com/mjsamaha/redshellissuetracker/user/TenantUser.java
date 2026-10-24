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
@Table(name = "tenant_users", uniqueConstraints = @UniqueConstraint(columnNames = { "tenant_id", "email" }))
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
	
	protected TenantUser() {
    }

    public TenantUser(String email, String passwordHash, String displayName, StaffRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}