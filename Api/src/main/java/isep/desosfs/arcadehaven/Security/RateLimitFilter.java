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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiter applied to sensitive endpoints (login, register, file upload).
 * Each IP gets 20 requests per minute on protected paths before receiving 429.
 */
@Component
public class RateLimitFilter implements Filter {

    private static final int REQUESTS_PER_MINUTE = 20;

    private static final Set<String> RATE_LIMITED_PREFIXES = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/publisher/games"
    );

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        boolean isRateLimited = RATE_LIMITED_PREFIXES.stream().anyMatch(path::startsWith);

        if (isRateLimited) {
            String ip = resolveClientIp(httpRequest);
            Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

            if (!bucket.tryConsume(1)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Too many requests. Please slow down.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUESTS_PER_MINUTE)
                .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
