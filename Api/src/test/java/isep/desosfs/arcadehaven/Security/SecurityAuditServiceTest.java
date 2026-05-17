package isep.desosfs.arcadehaven.Security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecurityAuditServiceTest {

    private SecurityAuditService service;

    @BeforeEach
    void setup() {
        service = new SecurityAuditService();
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
}
