package isep.desosfs.arcadehaven.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * V1.3.6 — SSRF protection: rejects RestTemplate calls to hosts not in the
 * application-level egress allowlist.
 * Defence-in-depth on top of Docker keycloak-internal network isolation.
 * Permitted hosts are configured at construction time from:
 *   - ${keycloak.server-url} host (JWT verification, admin API, login)
 *   - api.rawg.io (RF-12 — RAWG game-metadata enrichment)
 */
public class EgressAllowlistInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(EgressAllowlistInterceptor.class);

    private final Set<String> allowedHosts;

    public EgressAllowlistInterceptor(Set<String> allowedHosts) {
        this.allowedHosts = allowedHosts.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String host = request.getURI().getHost();
        if (host == null || !allowedHosts.contains(host.toLowerCase())) {
            log.error("SSRF-guard (V1.3.6): outbound request to '{}' blocked — not in egress allowlist {}",
                    host, allowedHosts);
            throw new IOException("SSRF-guard: egress to '" + host + "' is not permitted (V1.3.6)");
        }
        return execution.execute(request, body);
    }
}
