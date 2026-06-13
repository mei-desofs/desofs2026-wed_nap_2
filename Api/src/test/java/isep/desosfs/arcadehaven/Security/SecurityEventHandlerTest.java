package isep.desosfs.arcadehaven.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecurityEventHandlerTest {
    private SecurityAuditService auditService;
    private SecurityEventHandler handler;

    @BeforeEach
    void setup() {
        auditService = mock(SecurityAuditService.class);
        handler = new SecurityEventHandler(auditService);
    }

    @Test
    void testCommenceUnauthorized_writesJsonAndRecordsAudit() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");

        handler.commence(request, response, mock(AuthenticationException.class));

        verify(auditService).recordUnauthorized("1.2.3.4", "/api/test", "GET");
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");

        String json = sw.toString();
        assertTrue(json.contains("\"status\":401"));
        assertTrue(json.contains("Authentication required"));
    }

    @Test
    void testHandleAccessDenied_authenticatedUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        when(request.getRequestURI()).thenReturn("/api/admin");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn("5.6.7.8");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("johndoe");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        handler.handle(request, response, mock(AccessDeniedException.class));

        verify(auditService).recordAccessDenied("5.6.7.8", "/api/admin", "POST", "johndoe");
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType("application/json");

        String json = sw.toString();
        assertTrue(json.contains("\"status\":403"));
        assertTrue(json.contains("Access denied"));
    }

    @Test
    void testHandleAccessDenied_anonymousUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        when(request.getRequestURI()).thenReturn("/api/admin");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");

        SecurityContextHolder.clearContext();

        handler.handle(request, response, mock(AccessDeniedException.class));

        verify(auditService).recordAccessDenied("9.9.9.9", "/api/admin", "POST", "anonymous");
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType("application/json");

        String json = sw.toString();
        assertTrue(json.contains("\"status\":403"));
        assertTrue(json.contains("Access denied"));
    }

    @Test
    void testCommenceUsesXForwardedFor() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        handler.commence(request, response, mock(AuthenticationException.class));

        verify(auditService).recordUnauthorized("10.0.0.1", "/api/test", "GET");
    }
}
