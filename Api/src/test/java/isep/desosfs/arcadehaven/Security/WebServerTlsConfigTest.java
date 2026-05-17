package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WebServerTlsConfig — ASVS V12.1.1 / V12.1.2.
 * Verifies that only TLS 1.2 and 1.3 are permitted and that cipher suites
 * restricted to ECDHE (forward-secrecy) suites only.
 */
class WebServerTlsConfigTest {

    // ── Allowed protocols (V12.1.1) ───────────────────────────────────────────────

    @Test
    void enabledProtocols_includesTls12() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).contains("TLSv1.2");
    }

    @Test
    void enabledProtocols_includesTls13() {
        assertThat(WebServerTlsConfig.ENABLED_TLS_PROTOCOLS).contains("TLSv1.3");
    }

    // ── Forbidden protocols (V12.1.1) ─────────────────────────────────────────────

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

    // ── Approved cipher suites (V12.1.2) ─────────────────────────────────────────

    @Test
    void approvedCiphers_containsEcdheEcdsaAes256() {
        assertThat(WebServerTlsConfig.APPROVED_CIPHER_SUITES)
                .contains("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
    }

    @Test
    void approvedCiphers_containsEcdheRsaAes256() {
        assertThat(WebServerTlsConfig.APPROVED_CIPHER_SUITES)
                .contains("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
    }

    @Test
    void approvedCiphers_allUseEcdheForForwardSecrecy() {
        for (String cipher : WebServerTlsConfig.APPROVED_CIPHER_SUITES) {
            assertThat(cipher).as("Cipher %s must use ECDHE for forward secrecy", cipher)
                    .contains("ECDHE");
        }
    }

    @Test
    void approvedCiphers_allUseAeadMode() {
        // Only GCM (AEAD) is approved — CBC without AEAD is excluded
        for (String cipher : WebServerTlsConfig.APPROVED_CIPHER_SUITES) {
            assertThat(cipher).as("Cipher %s must use GCM (AEAD)", cipher)
                    .contains("GCM");
        }
    }

    // ── Forbidden cipher suites (V12.1.2) ────────────────────────────────────────

    @Test
    void forbiddenCiphers_doNotOverlapWithApprovedCiphers() {
        for (String forbidden : WebServerTlsConfig.FORBIDDEN_CIPHER_SUITES) {
            assertThat(WebServerTlsConfig.APPROVED_CIPHER_SUITES)
                    .as("Forbidden cipher %s must not appear in approved list", forbidden)
                    .doesNotContain(forbidden);
        }
    }

    @Test
    void forbiddenCiphers_includesNonForwardSecrecySuites() {
        assertThat(WebServerTlsConfig.FORBIDDEN_CIPHER_SUITES)
                .contains("TLS_RSA_WITH_AES_256_CBC_SHA256");
    }

    @Test
    void forbiddenCiphers_includesRc4() {
        assertThat(WebServerTlsConfig.FORBIDDEN_CIPHER_SUITES)
                .contains("SSL_RSA_WITH_RC4_128_SHA");
    }

    @Test
    void forbiddenCiphers_includes3des() {
        assertThat(WebServerTlsConfig.FORBIDDEN_CIPHER_SUITES)
                .contains("TLS_RSA_WITH_3DES_EDE_CBC_SHA");
    }
}
