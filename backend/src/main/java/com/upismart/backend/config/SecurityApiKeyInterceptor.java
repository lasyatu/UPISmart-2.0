package com.upismart.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Application-level security interceptor for protected REST API endpoints.
 *
 * Enforces authentication on state-mutating requests (POST, PUT, DELETE) using:
 * 1. 'X-API-KEY' header matching the configured secure secret, OR
 * 2. Same-origin browser requests from the embedded Web Dashboard.
 *
 * Public read-only endpoints (GET /api/health, GET /api/budget/utilization, etc.)
 * remain accessible for dashboard metrics and health checks.
 */
@Component
public class SecurityApiKeyInterceptor implements HandlerInterceptor {

    public static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${app.security.api-key:upismart-demo-secure-key-2026}")
    private String configuredApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();

        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow public read-only requests
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }

        // For state-mutating requests (POST, PUT, DELETE), verify API key or same-origin
        String providedKey = request.getHeader(API_KEY_HEADER);
        if (providedKey != null && providedKey.equals(configuredApiKey)) {
            return true;
        }

        // Allow same-origin requests from the embedded web dashboard
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if ("same-origin".equalsIgnoreCase(fetchSite)) {
            return true;
        }

        // Check Origin / Referer against host for standard web dashboard forms
        String host = request.getHeader("Host");
        String origin = request.getHeader("Origin");
        if (host != null && origin != null && origin.contains(host)) {
            return true;
        }

        // Authentication failed
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"errorCode\":\"UNAUTHORIZED_API_ACCESS\",\"message\":\"Missing or invalid X-API-KEY header for protected endpoint.\"}");
        return false;
    }
}
