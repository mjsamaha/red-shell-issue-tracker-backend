package com.mjsamaha.redshellissuetracker.tenant;

import java.util.UUID;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        return TenantContext.get();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}