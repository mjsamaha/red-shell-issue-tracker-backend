package com.mjsamaha.redshellissuetracker.tenant;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mjsamaha.redshellissuetracker.tenant.dto.CreateTenantRequest;
import com.mjsamaha.redshellissuetracker.tenant.dto.TenantResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/platform/v1/tenants")
@Profile("dev")
public class PlatformTenantController {
    private final TenantService tenants;

    public PlatformTenantController(TenantService tenants) { this.tenants = tenants; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse create(@Valid @RequestBody CreateTenantRequest request) {
        return tenants.create(request);
    }

    @PostMapping("/{id}/suspend")
    public TenantResponse suspend(@PathVariable UUID id) { return tenants.suspend(id); }

    @PostMapping("/{id}/reactivate")
    public TenantResponse reactivate(@PathVariable UUID id) { return tenants.reactivate(id); }
}
