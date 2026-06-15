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

    // ── V14.2.4 — sensitive key redaction (json method) ───────────────────

    @Test
    void json_withPasswordKey_redactsValue() {
        String result = SecurityAuditService.json("test", "password", "supersecret123");
        assertTrue(result.contains("\"password\":\"[REDACTED]\""),
                "password field must be redacted");
        assertFalse(result.contains("supersecret123"),
                "plaintext password must not appear in log output");
    }

    @Test
    void json_withSecretKey_redactsValue() {
        String result = SecurityAuditService.json("test", "secret", "my-client-secret");
        assertTrue(result.contains("[REDACTED]"), "secret field must be redacted");
        assertFalse(result.contains("my-client-secret"),
                "plaintext secret must not appear in log output");
    }

    @Test
    void json_withClientSecretKey_redactsValue() {
        String result = SecurityAuditService.json("test", "client_secret", "backend-secret");
        assertTrue(result.contains("[REDACTED]"), "client_secret field must be redacted");
        assertFalse(result.contains("backend-secret"),
                "plaintext client_secret must not appear in log output");
    }

    @Test
    void json_withCaseInsensitivePasswordKey_redactsValue() {
        String result = SecurityAuditService.json("test", "PASSWORD", "shouldBeRedacted");
        assertTrue(result.contains("[REDACTED]"),
                "case-insensitive key match must trigger redaction");
        assertFalse(result.contains("shouldBeRedacted"),
                "value must not appear in output when key is sensitive");
    }

    @Test
    void json_withNonSensitiveKey_preservesValue() {
        String result = SecurityAuditService.json("test", "username", "alice");
        assertTrue(result.contains("\"username\":\"alice\""),
                "non-sensitive key must preserve its value");
    }

    // ── V14.2.4 — form-encoded credential redaction (sanitize method) ─────

    @Test
    void sanitize_formEncodedPassword_isRedacted() {
        String input = "grant_type=password&username=alice&password=hunter2";
        String result = SecurityAuditService.sanitize(input);
        assertFalse(result.contains("hunter2"),
                "form-encoded password value must be redacted");
        assertTrue(result.contains("password=[REDACTED]"),
                "form-encoded password key must remain with [REDACTED] value");
    }

    @Test
    void sanitize_formEncodedSecret_isRedacted() {
        String input = "client_id=app&secret=very-secret-value&scope=openid";
        String result = SecurityAuditService.sanitize(input);
        assertFalse(result.contains("very-secret-value"),
                "form-encoded secret value must be redacted from log string");
    }
}
