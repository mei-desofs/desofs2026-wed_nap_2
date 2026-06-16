package isep.desosfs.arcadehaven.Security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP, per-tier rate limiter applied to all high-risk endpoints.
 *
 * Three tiers with independent buckets per IP (ASVS V2.4.1):
 *   AUTH   — 20 req/min  : POST /api/auth/login, /api/auth/register
 *   UPLOAD — 10 req/min  : POST /api/publisher/games (file upload)
 *   STATE  —  5 req/min  : POST /api/orders/{id}/complete, /api/library/import-key
 *
 * Bucket key = "IP::TIER_NAME" — each tier is throttled independently;
 * exhausting STATE does not affect AUTH headroom and vice-versa.
 * Rate-limit breaches are recorded as security events (ASVS V16.3.3).
 */
@Component
public class RateLimitFilter implements Filter {

    private enum RateTier {
        AUTH(20),    // Authentication endpoints
        UPLOAD(10),  // File-upload / game submission
        STATE(5);    // Business state-changing operations (payment, key import)

        final int requestsPerMinute;

        RateTier(int rpm) {
            this.requestsPerMinute = rpm;
        }
    }

    // Ordered: first matching prefix wins — more-specific paths listed first
    private static final Map<String, RateTier> PREFIX_TIERS;
    static {
        PREFIX_TIERS = new LinkedHashMap<>();
        PREFIX_TIERS.put("/api/auth/login",         RateTier.AUTH);
        PREFIX_TIERS.put("/api/auth/register",       RateTier.AUTH);
        PREFIX_TIERS.put("/api/publisher/games",     RateTier.UPLOAD);
        PREFIX_TIERS.put("/api/orders",              RateTier.STATE);  // covers /{id}/complete
        PREFIX_TIERS.put("/api/library/import-key",  RateTier.STATE);
    }

    // Key: "IP::TIER_NAME" — separate bucket per IP per tier
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final SecurityAuditService auditService;

    public RateLimitFilter(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        RateTier tier = resolveTier(path);

        if (tier != null) {
            String ip = resolveClientIp(httpRequest);
            String bucketKey = ip + "::" + tier.name();
            Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> newBucket(tier));

            if (!bucket.tryConsume(1)) {
                auditService.recordRateLimitExceeded(ip, path);
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Too many requests. Please slow down.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private RateTier resolveTier(String path) {
        for (Map.Entry<String, RateTier> entry : PREFIX_TIERS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Bucket newBucket(RateTier tier) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(tier.requestsPerMinute)
                .refillGreedy(tier.requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    // ASVS V4.1.3 / V15.3.4
    private String resolveClientIp(HttpServletRequest request) {
        // Tomcat's RemoteIpFilter (server.forward-headers-strategy=NATIVE) has already
        // resolved the real client IP from trusted X-Forwarded-For headers before this
        // filter runs. Reading the header directly here would allow spoofing.
        return request.getRemoteAddr();
    }
}
