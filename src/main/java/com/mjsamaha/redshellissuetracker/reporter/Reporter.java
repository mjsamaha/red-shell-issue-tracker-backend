package com.mjsamaha.redshellissuetracker.reporter;

import java.time.Instant;

import com.mjsamaha.redshellissuetracker.common.TenantScopedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

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
    
    protected Reporter() {
    }

    public Reporter(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getVerificationTokenHash() {
        return verificationTokenHash;
    }

    public void setVerificationTokenHash(String verificationTokenHash) {
        this.verificationTokenHash = verificationTokenHash;
    }

    public Instant getVerificationExpiresAt() {
        return verificationExpiresAt;
    }

    public void setVerificationExpiresAt(Instant verificationExpiresAt) {
        this.verificationExpiresAt = verificationExpiresAt;
    }
}