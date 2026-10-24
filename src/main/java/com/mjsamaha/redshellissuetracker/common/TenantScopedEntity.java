package com.mjsamaha.redshellissuetracker.common;

import java.util.UUID;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Column;

@MappedSuperclass
public abstract class TenantScopedEntity extends BaseEntity {

	@TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;
	
}
