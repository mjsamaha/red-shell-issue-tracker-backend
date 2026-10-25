package com.mjsamaha.redshellissuetracker.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class ProjectKeyLookup {
    private final JdbcClient jdbc;

    public ProjectKeyLookup(JdbcClient jdbc) { this.jdbc = jdbc; }

    public Optional<UUID> findTenantId(String projectKey) {
        return jdbc.sql("select tenant_id from applications where project_key = :projectKey and archived_at is null")
                .param("projectKey", projectKey)
                .query(UUID.class)
                .optional();
    }
}
