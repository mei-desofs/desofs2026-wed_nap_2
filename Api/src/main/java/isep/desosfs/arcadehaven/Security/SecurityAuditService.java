package isep.desosfs.arcadehaven.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Records structured security events and fires alerts when per-IP failure
 * thresholds are breached within a rolling time window.
 *
 * RNF-05: every 401/403, validation failure, rate-limit hit, and admin action is
 *         written as a JSON SECURITY_EVENT log line (ASVS V16.2.4).
 * RNF-06: when an IP exceeds ALERT_THRESHOLD events inside WINDOW_SECONDS a
 *         SECURITY_ALERT line is written (scrape-able by any log aggregator).
 * V16.4.1: all values are stripped of CR/LF/tab before logging (log-injection prevention).
 */
@Service
public class SecurityAuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private static final int ALERT_THRESHOLD = 10;
    private static final long WINDOW_SECONDS = 60;

    // V16.2.5 — patterns for automatic sensitive-value redaction in log output
    private static final Pattern JWT_PATTERN =
            Pattern.compile("eyJ[A-Za-z0-9+/\\-_]+=*\\.[A-Za-z0-9+/\\-_]+=*\\.[A-Za-z0-9+/\\-_]+=*");
    private static final Pattern ACTIVATION_KEY_PATTERN =
            Pattern.compile("\\b[A-F0-9]{32}\\b");
    // V14.2.4 — key-based redaction: any log field whose key matches this pattern
    //           has its value replaced with [REDACTED] to prevent accidental
    //           credential disclosure (passwords, secrets, client credentials).
    private static final Pattern SENSITIVE_KEY_PATTERN =
            Pattern.compile("(?i)\\b(password|secret|credential|private_key|client_secret)\\b");
    // V14.2.4 — form-encoded credential pattern (e.g. "password=abc123" embedded in
    //           error messages or request-body echo in exception strings).
    private static final Pattern FORM_ENCODED_SECRET_PATTERN =
            Pattern.compile("(?i)(password|secret|credential)=[^&\\s\"'\\}]+");

    private final ConcurrentHashMap<String, IpWindow> windows = new ConcurrentHashMap<>();

    // ── Authentication events (V16.3.1) ─────────────────────────────────────

    public void recordUnauthorized(String ip, String path, String method) {
        auditLog.warn(json("SECURITY_EVENT", "type", "UNAUTHORIZED",
                "ip", ip, "method", method, "path", path));
        checkAndAlert(ip, "UNAUTHORIZED");
    }

    public void recordAccessDenied(String ip, String path, String method, String username) {
        auditLog.warn(json("SECURITY_EVENT", "type", "ACCESS_DENIED",
                "ip", ip, "method", method, "path", path, "user", username));
        checkAndAlert(ip, "ACCESS_DENIED");
    }

    /** Logs a successful login. IP is resolved from the current request context. */
    public void recordLoginSuccess(String username) {
        auditLog.info(json("SECURITY_EVENT", "type", "LOGIN_SUCCESS",
                "ip", resolveCurrentIp(), "user", username));
    }

    /** Logs a failed registration attempt. */
    public void recordRegistrationFailure(String ip, String reason) {
        auditLog.warn(json("SECURITY_EVENT", "type", "REGISTRATION_FAILURE",
                "ip", ip, "reason", reason));
        checkAndAlert(ip, "REGISTRATION_FAILURE");
    }

    /** Logs a successful registration. IP is resolved from the current request context. */
    public void recordRegistrationSuccess(String username) {
        auditLog.info(json("SECURITY_EVENT", "type", "REGISTRATION_SUCCESS",
                "ip", resolveCurrentIp(), "user", username));
    }

    // ── Admin audit events (RNF-13) ──────────────────────────────────────────

    /**
     * Logs a privileged admin action so that role changes, account toggles,
     * and library modifications are fully traceable (ASVS V16.2.1 — who/what/when).
     */
    public void recordAdminAction(String adminUsername, String action, String target) {
        auditLog.info(json("SECURITY_EVENT", "type", "ADMIN_ACTION",
                "admin", adminUsername, "action", action, "target", target));
    }

    // ── Anti-automation events (V16.3.3) ─────────────────────────────────────

    /** Logs a per-IP rate-limit breach on a protected endpoint. */
    public void recordRateLimitExceeded(String ip, String path) {
        auditLog.warn(json("SECURITY_EVENT", "type", "RATE_LIMIT_EXCEEDED",
                "ip", ip, "path", path));
        checkAndAlert(ip, "RATE_LIMIT_EXCEEDED");
    }

    /** Logs an input-validation failure as a security event. */
    public void recordValidationFailure(String ip, String path, String details) {
        auditLog.warn(json("SECURITY_EVENT", "type", "VALIDATION_FAILURE",
                "ip", ip, "path", path, "details", details));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void checkAndAlert(String ip, String eventType) {
        IpWindow window = windows.computeIfAbsent(ip, k -> new IpWindow());
        int count = window.increment();
        if (count == ALERT_THRESHOLD) {
            auditLog.error(json("SECURITY_ALERT", "type", "THRESHOLD_EXCEEDED",
                    "ip", ip, "event", eventType,
                    "count", String.valueOf(count),
                    "window_sec", String.valueOf(WINDOW_SECONDS)));
        }
    }

    /** Resolves the client IP from the current HTTP request context (falls back to "unknown"). */
    // ASVS 4.1.3
    private static String resolveCurrentIp() {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                HttpServletRequest req = sra.getRequest();
                return req.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    /**
     * Strips CR, LF and TAB (V16.4.1) and redacts JWT tokens, 32-char hex
     * activation keys, and form-encoded credentials before embedding any value
     * in a JSON log line (V16.2.5 / V14.2.4).
     */
    static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\r", "").replace("\n", "").replace("\t", "").replace("\"", "'");
        v = JWT_PATTERN.matcher(v).replaceAll("[JWT_REDACTED]");
        v = ACTIVATION_KEY_PATTERN.matcher(v).replaceAll("[KEY_REDACTED]");
        v = FORM_ENCODED_SECRET_PATTERN.matcher(v).replaceAll("$1=[REDACTED]");
        return v;
    }

    /**
     * Builds a JSON-structured log entry (ASVS V16.2.4).
     * Format: {"event":"<name>","ts":"<iso>","key":"value",...}
     *
     * V14.2.4: any key whose name matches SENSITIVE_KEY_PATTERN (password, secret,
     * credential, private_key, client_secret) has its value replaced with [REDACTED]
     * regardless of the value itself, preventing accidental credential disclosure.
     */
    static String json(String event, String... kvPairs) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"event\":\"").append(sanitize(event)).append("\"");
        sb.append(",\"ts\":\"").append(Instant.now()).append("\"");
        for (int i = 0; i + 1 < kvPairs.length; i += 2) {
            String key = sanitize(kvPairs[i]);
            String value = SENSITIVE_KEY_PATTERN.matcher(kvPairs[i]).find()
                    ? "[REDACTED]"
                    : sanitize(kvPairs[i + 1]);
            sb.append(",\"").append(key).append("\":\"").append(value).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    @Scheduled(fixedDelay = 60_000)
    public void evictStaleWindows() {
        long cutoff = Instant.now().getEpochSecond() - WINDOW_SECONDS;
        windows.entrySet().removeIf(e -> e.getValue().windowStart() < cutoff);
    }

    private static final class IpWindow {
        private final long start = Instant.now().getEpochSecond();
        private final AtomicInteger count = new AtomicInteger(0);

        int increment() {
            return count.incrementAndGet();
        }

        long windowStart() {
            return start;
        }
    }
}
