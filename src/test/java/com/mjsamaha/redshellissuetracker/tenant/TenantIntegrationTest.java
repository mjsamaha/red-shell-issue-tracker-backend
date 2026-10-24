package com.mjsamaha.redshellissuetracker.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mjsamaha.redshellissuetracker.PostgresIntegrationTest;

class TenantIntegrationTest extends PostgresIntegrationTest {
    @Autowired TenantRepository tenants;
    TenantTestSupport tenantTestSupport;

    @org.junit.jupiter.api.BeforeEach
    void setUpHelper() {
        tenantTestSupport = new TenantTestSupport(tenants);
    }

    @AfterEach
    void clearTenantContext() {
        tenantTestSupport.clearContext();
    }

    @Test
    void createsAndReadsTenantAgainstPostgres() {
        Tenant created = tenantTestSupport.createAndSetContext("phase-zero", "Phase Zero");
        Tenant loaded = tenants.findBySlug("phase-zero").orElseThrow();
        assertThat(loaded.getId()).isEqualTo(created.getId());
        assertThat(loaded.getName()).isEqualTo("Phase Zero");
        assertThat(TenantContext.get()).isEqualTo(created.getId());
    }
}
