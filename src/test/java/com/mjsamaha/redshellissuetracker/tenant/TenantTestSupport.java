package com.mjsamaha.redshellissuetracker.tenant;

public class TenantTestSupport {
    private final TenantRepository tenants;

    public TenantTestSupport(TenantRepository tenants) {
        this.tenants = tenants;
    }

    public Tenant createAndSetContext(String slug, String name) {
        Tenant tenant = tenants.saveAndFlush(new Tenant(slug, name));
        TenantContext.set(tenant.getId());
        return tenant;
    }

    public void clearContext() {
        TenantContext.clear();
    }
}
