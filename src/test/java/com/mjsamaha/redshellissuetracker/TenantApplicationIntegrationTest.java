package com.mjsamaha.redshellissuetracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mjsamaha.redshellissuetracker.application.ApplicationService;
import com.mjsamaha.redshellissuetracker.application.dto.CreateApplicationRequest;
import com.mjsamaha.redshellissuetracker.tenant.Tenant;
import com.mjsamaha.redshellissuetracker.tenant.TenantContext;
import com.mjsamaha.redshellissuetracker.tenant.TenantRepository;

@ActiveProfiles("dev")
@AutoConfigureMockMvc
class TenantApplicationIntegrationTest extends PostgresIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired TenantRepository tenants;
    @Autowired ApplicationService applications;

    @AfterEach
    void clearContext() { TenantContext.clear(); }

    @Test
    void publicEndpointsExposeOnlyTenantSafeData() throws Exception {
        mvc.perform(post("/platform/v1/tenants")
                        .contentType("application/json")
                        .content("""
                                {"slug":"red-shell","name":"Red Shell","ownerEmail":"owner@example.com","ownerDisplayName":"Owner"}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(post("/platform/v1/tenants")
                        .contentType("application/json")
                        .content("""
                                {"slug":"blue-shell","name":"Blue Shell","ownerEmail":"owner@example.com","ownerDisplayName":"Owner"}
                                """))
                .andExpect(status().isCreated());

        createApplication("red-shell", "red-app");
        createApplication("blue-shell", "blue-app");

        mvc.perform(get("/public/red-shell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Red Shell"));
        mvc.perform(get("/public/red-shell/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("red-app"))
                .andExpect(jsonPath("$[0].projectKey").doesNotExist());
        mvc.perform(get("/public/blue-shell/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("blue-app"));
    }

    private void createApplication(String tenantSlug, String appSlug) {
        Tenant tenant = tenants.findBySlug(tenantSlug).orElseThrow();
        TenantContext.set(tenant.getId());
        try {
            applications.create(new CreateApplicationRequest(appSlug, appSlug, null, null, true, true, true));
        } finally {
            TenantContext.clear();
        }
    }
}
