package isep.desosfs.arcadehaven.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records structured security events and fires alerts when per-IP failure
 * thresholds are breached within a rolling time window.
 *
 * RNF-05: every 401 / 403 is written as a SECURITY_EVENT log line.
 * RNF-06: when an IP exceeds ALERT_THRESHOLD events inside WINDOW_SECONDS
 *         a SECURITY_ALERT line is written (scrape-able by any log aggregator).
 */
@Service
public class SecurityAuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private static final int ALERT_THRESHOLD = 10;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, IpWindow> windows = new ConcurrentHashMap<>();

    public void recordUnauthorized(String ip, String path, String method) {
        auditLog.warn("SECURITY_EVENT | type=UNAUTHORIZED | ip={} | method={} | path={}",
                ip, method, path);
        checkAndAlert(ip, "UNAUTHORIZED");
    }

    public void recordAccessDenied(String ip, String path, String method, String username) {
        auditLog.warn("SECURITY_EVENT | type=ACCESS_DENIED | ip={} | method={} | path={} | user={}",
                ip, method, path, username);
        checkAndAlert(ip, "ACCESS_DENIED");
    }

    public void recordRegistrationFailure(String ip, String reason) {
        auditLog.warn("SECURITY_EVENT | type=REGISTRATION_FAILURE | ip={} | reason={}", ip, reason);
        checkAndAlert(ip, "REGISTRATION_FAILURE");
    }

    private void checkAndAlert(String ip, String eventType) {
        IpWindow window = windows.computeIfAbsent(ip, k -> new IpWindow());
        int count = window.increment();
        if (count == ALERT_THRESHOLD) {
            auditLog.error(
                    "SECURITY_ALERT | type=THRESHOLD_EXCEEDED | ip={} | event={} | count={} | window={}s",
                    ip, eventType, count, WINDOW_SECONDS);
        }
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
