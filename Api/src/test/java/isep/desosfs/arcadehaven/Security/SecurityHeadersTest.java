package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for SecurityConfig (ASVS V3.4.2 — CORS origin allowlist).
 *
 * Note: HSTS (V3.4.1) and X-Content-Type-Options (V3.4.4) are enforced
 * by Spring Security's HeadersConfigurer, which is configured in
 * SecurityConfig.filterChain(). The Spring Security team tests that
 * layer; here we verify our CORS allowlist logic because it contains
 * application-specific business rules (which origins are trusted).
 */
class SecurityHeadersTest {

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        SecurityEventHandler handler = mock(SecurityEventHandler.class);
        config = new SecurityConfig(handler);
        ReflectionTestUtils.setField(config, "allowedOrigins",
                List.of("http://localhost:3000", "http://localhost:5173"));
    }

    // ── ASVS V3.4.2 — CORS: allowed origins ─────────────────────────────────────

    @Test
    void corsConfig_firstAllowedOrigin_isAccepted() {
        assertThat(getCors().checkOrigin("http://localhost:3000"))
                .isEqualTo("http://localhost:3000");
    }

    @Test
    void corsConfig_secondAllowedOrigin_isAccepted() {
        assertThat(getCors().checkOrigin("http://localhost:5173"))
                .isEqualTo("http://localhost:5173");
    }

    // ── ASVS V3.4.2 — CORS: disallowed origins ──────────────────────────────────

    @Test
    void corsConfig_arbitraryOrigin_isRejected() {
        assertThat(getCors().checkOrigin("http://evil.com")).isNull();
    }

    @Test
    void corsConfig_wildcardOrigin_isRejected() {
        assertThat(getCors().checkOrigin("*")).isNull();
    }

    @Test
    void corsConfig_subdomainOfAllowedHost_isRejected() {
        // http://sub.localhost:3000 is NOT the same as http://localhost:3000
        assertThat(getCors().checkOrigin("http://sub.localhost:3000")).isNull();
    }

    @Test
    void corsConfig_httpVsHttpsVariant_isRejected() {
        // https://localhost:3000 must not be accepted when only http is in the list
        assertThat(getCors().checkOrigin("https://localhost:3000")).isNull();
    }

    // ── ASVS V3.4.2 — CORS: method and header coverage ──────────────────────────

    @Test
    void corsConfig_allowedMethods_includeAllRequiredVerbs() {
        assertThat(getCors().getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void corsConfig_allowedHeaders_includesAuthorization() {
        assertThat(getCors().getAllowedHeaders()).contains("Authorization");
    }

    @Test
    void corsConfig_allowedHeaders_includesContentType() {
        assertThat(getCors().getAllowedHeaders()).contains("Content-Type");
    }

    // ── Coverage: configuration is applied globally ──────────────────────────────

    @Test
    void corsConfigSource_returnsConfigForAnyPath() {
        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/publisher/games");
        assertThat(source.getCorsConfiguration(req)).isNotNull();
    }

    // ── ASVS V14.2.2 / V14.3.2 — Cache-Control ──────────────────────────────────

    @Test
    void cacheControl_setsNoCacheHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CacheControlHeadersWriter().writeHeaders(new MockHttpServletRequest(), response);
        assertThat(response.getHeader("Cache-Control")).contains("no-cache");
    }

    @Test
    void cacheControl_setsNoStoreHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CacheControlHeadersWriter().writeHeaders(new MockHttpServletRequest(), response);
        assertThat(response.getHeader("Cache-Control")).contains("no-store");
    }

    @Test
    void cacheControl_setsPragmaNoCache() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CacheControlHeadersWriter().writeHeaders(new MockHttpServletRequest(), response);
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
    }

    @Test
    void cacheControl_setsMaxAgeZero() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CacheControlHeadersWriter().writeHeaders(new MockHttpServletRequest(), response);
        assertThat(response.getHeader("Cache-Control")).contains("max-age=0");
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private CorsConfiguration getCors() {
        return config.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest());
    }
}
