package com.mjsamaha.redshellissuetracker.tenant;

import java.util.UUID;

public final class TenantContext {

    // Matches no real tenant. Used outside requests (startup, background work).
    public static final UUID NO_TENANT = new UUID(0L, 0L);

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID tenantId) {
        CURRENT.set(tenantId);
    }

    public static UUID get() {
        UUID id = CURRENT.get();
        return id != null ? id : NO_TENANT;
    }

    public static void clear() {
        CURRENT.remove();
    }
}