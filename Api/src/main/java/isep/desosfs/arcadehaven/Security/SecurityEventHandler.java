package isep.desosfs.arcadehaven.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Single handler wired into Spring Security for both 401 and 403 responses.
 * Delegates to SecurityAuditService for structured logging and threshold alerting.
 */
@Component
public class SecurityEventHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final SecurityAuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityEventHandler(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    /** 401 — no valid token present or token is expired/malformed. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        auditService.recordUnauthorized(
                resolveIp(request),
                request.getRequestURI(),
                request.getMethod());
        writeJson(response, HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    /** 403 — valid token, but the principal lacks the required role. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String username = resolveUsername();
        auditService.recordAccessDenied(
                resolveIp(request),
                request.getRequestURI(),
                request.getMethod(),
                username);
        writeJson(response, HttpStatus.FORBIDDEN, "Access denied");
    }

    private String resolveUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeJson(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("status", status.value(), "error", message));
    }
}
