package com.mjsamaha.redshellissuetracker.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mjsamaha.redshellissuetracker.tenant.dto.TenantBrandingResponse;
import com.mjsamaha.redshellissuetracker.tenant.dto.TenantResponse;
import com.mjsamaha.redshellissuetracker.tenant.dto.UpdateTenantSettingsRequest;

import jakarta.validation.Valid;

@RestController
public class TenantController {
    private final TenantService tenants;

    public TenantController(TenantService tenants) { this.tenants = tenants; }

    @GetMapping("/public/{tenantSlug}")
    public TenantBrandingResponse branding(@PathVariable String tenantSlug) {
        return tenants.getBranding(tenantSlug);
    }

    @GetMapping("/api/v1/tenant")
    public TenantResponse currentTenant() { return tenants.getCurrent(); }

    @PutMapping("/api/v1/tenant/settings")
    public TenantResponse updateSettings(@Valid @RequestBody UpdateTenantSettingsRequest request) {
        return tenants.updateSettings(request);
    }
}
