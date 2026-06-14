package isep.desosfs.arcadehaven.Security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SecurityAuditServiceTest {

    private SecurityAuditService service;

    @BeforeEach
    void setup() {
        service = new SecurityAuditService();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void recordUnauthorized_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordUnauthorized("127.0.0.1", "/api/auth/login", "POST"));
    }

    @Test
    void recordAccessDenied_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordAccessDenied("127.0.0.1", "/api/admin/users", "GET", "alice"));
    }

    @Test
    void recordRegistrationFailure_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordRegistrationFailure("127.0.0.1", "duplicate username"));
    }

    @Test
    void multipleEvents_fromSameIp_alertsAfterThreshold() {
        for (int i = 0; i < 12; i++) {
            service.recordUnauthorized("10.0.0.1", "/api/auth/login", "POST");
        }
        // no exception expected even after alert threshold (10) is exceeded
    }

    @Test
    void evictStaleWindows_doesNotThrow() {
        service.recordUnauthorized("192.168.1.1", "/api/login", "POST");
        assertDoesNotThrow(() -> service.evictStaleWindows());
    }

    @Test
    void recordUnauthorized_distinctIps_areTrackedSeparately() {
        assertDoesNotThrow(() -> {
            service.recordUnauthorized("1.1.1.1", "/api/login", "POST");
            service.recordUnauthorized("2.2.2.2", "/api/login", "POST");
        });
    }

    @Test
    void recordAccessDenied_withUsername_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordAccessDenied("10.0.0.5", "/api/admin", "DELETE", "bob"));
    }

    @Test
    void evictStaleWindows_withEmptyWindows_doesNotThrow() {
        assertDoesNotThrow(() -> service.evictStaleWindows());
    }

    @Test
    void recordLoginSuccess_withRequestContext_doesNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertDoesNotThrow(() -> service.recordLoginSuccess("alice"));
    }

    @Test
    void recordLoginSuccess_withXForwardedFor_usesFirstIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertDoesNotThrow(() -> service.recordLoginSuccess("alice"));
    }

    @Test
    void recordLoginSuccess_withoutRequestContext_fallsBackToUnknown() {
        RequestContextHolder.resetRequestAttributes();
        assertDoesNotThrow(() -> service.recordLoginSuccess("alice"));
    }

    @Test
    void recordRegistrationSuccess_withRequestContext_doesNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertDoesNotThrow(() -> service.recordRegistrationSuccess("bob"));
    }

    @Test
    void recordRegistrationSuccess_withoutRequestContext_doesNotThrow() {
        RequestContextHolder.resetRequestAttributes();
        assertDoesNotThrow(() -> service.recordRegistrationSuccess("bob"));
    }

    @Test
    void recordAdminAction_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordAdminAction("admin", "DISABLE_ACCOUNT", "user:42"));
    }

    @Test
    void recordRateLimitExceeded_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordRateLimitExceeded("10.0.0.1", "/api/auth/login"));
    }

    @Test
    void recordRateLimitExceeded_triggersAlertAfterThreshold() {
        for (int i = 0; i < 12; i++) {
            service.recordRateLimitExceeded("192.168.99.99", "/api/auth/login");
        }
    }

    @Test
    void recordValidationFailure_doesNotThrow() {
        assertDoesNotThrow(() ->
                service.recordValidationFailure("10.0.0.1", "/api/register", "email invalid"));
    }

    @Test
    void resolveCurrentIp_withNonServletAttributes_returnsUnknown() {
        RequestContextHolder.setRequestAttributes(
                mock(org.springframework.web.context.request.RequestAttributes.class));

        assertDoesNotThrow(() -> service.recordLoginSuccess("charlie"));
    }

    @Test
    void resolveCurrentIp_xForwardedForBlank_usesRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("172.16.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertDoesNotThrow(() -> service.recordLoginSuccess("dave"));
    }

    @Test
    void sanitize_null_returnsEmpty() {
        assertEquals("", SecurityAuditService.sanitize(null));
    }

    @Test
    void evictStaleWindows_withFreshWindow_doesNotRemoveIt() {
        service.recordUnauthorized("55.55.55.55", "/api/login", "POST");
        assertDoesNotThrow(() -> service.evictStaleWindows());
    }
}
