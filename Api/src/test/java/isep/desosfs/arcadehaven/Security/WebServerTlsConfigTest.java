package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WebServerTlsConfig — ASVS V12.1.1.
 * Verifies that only TLS 1.2 and 1.3 are permitted; all weaker protocol
 * versions are explicitly excluded from the allowed protocol list.
 */
class WebServerTlsConfigTest {

    // ── Allowed protocols ────────────────────────────────────────────────────────

    @Test
    void enabledProtocols_includesTls12() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).contains("TLSv1.2");
    }

    @Test
    void enabledProtocols_includesTls13() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).contains("TLSv1.3");
    }

    // ── Forbidden protocols ──────────────────────────────────────────────────────

    @Test
    void enabledProtocols_excludesSslV2() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).doesNotContain("SSLv2");
    }

    @Test
    void enabledProtocols_excludesSslV3() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).doesNotContain("SSLv3");
    }

    @Test
    void enabledProtocols_excludesTls10() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).doesNotContain("TLSv1.0");
    }

    @Test
    void enabledProtocols_excludesTls11() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).doesNotContain("TLSv1.1");
    }

    @Test
    void forbiddenProtocols_containsAllWeakVersions() {
        assertThat(WebServerTlsConfig.FORBIDDEN_TLS_PROTOCOLS)
                .contains("SSLv2", "SSLv3", "TLSv1.0", "TLSv1.1");
    }

    @Test
    void forbiddenProtocols_doNotOverlapWithEnabledProtocols() {
        for (String forbidden : WebServerTlsConfig.FORBIDDEN_TLS_PROTOCOLS) {
            assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).doesNotContain(forbidden);
        }
    }
}
