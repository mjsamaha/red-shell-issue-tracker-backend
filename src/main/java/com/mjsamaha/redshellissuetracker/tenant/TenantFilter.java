package com.mjsamaha.redshellissuetracker.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mjsamaha.redshellissuetracker.common.TenantStatus;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Pattern PUBLIC_PATH = Pattern.compile("^/public/([a-z0-9-]+)(?:/.*)?$");

    private final TenantLookupService tenants;

    public TenantFilter(TenantLookupService tenants) {
        this.tenants = tenants;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Matcher m = PUBLIC_PATH.matcher(request.getRequestURI());
        if (m.matches()) {
            Optional<Tenant> tenant = tenants.findBySlug(m.group(1));
            if (tenant.isEmpty() || tenant.get().getStatus() != TenantStatus.ACTIVE) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            TenantContext.set(tenant.get().getId());
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
