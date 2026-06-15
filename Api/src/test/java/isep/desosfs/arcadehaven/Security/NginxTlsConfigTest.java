package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NginxTlsConfig — ASVS V12.1.1 / V12.1.2 / V12.2.1.
 *
 * Validates that the nginx reverse-proxy TLS policy constants (mirrored in
 * Api/nginx/nginx.conf) enforce forward-secrecy cipher suites, restrict
 * protocols to TLS 1.2/1.3, and define the correct listening ports for TLS
 * and HTTP-to-HTTPS redirect.
 */
class NginxTlsConfigTest {

    // ── Allowed protocols (V12.1.1) ───────────────────────────────────────────

    @Test
    void nginxProtocols_includesTls12() {
        assertThat(NginxTlsConfig.NGINX_SSL_PROTOCOLS).contains("TLSv1.2");
    }

    @Test
    void nginxProtocols_includesTls13() {
        assertThat(NginxTlsConfig.NGINX_SSL_PROTOCOLS).contains("TLSv1.3");
    }

    // ── Forbidden protocols (V12.1.1) ─────────────────────────────────────────

    @Test
    void nginxProtocols_excludesSslV2() {
        assertThat(NginxTlsConfig.NGINX_SSL_PROTOCOLS).doesNotContain("SSLv2");
    }

    @Test
    void nginxProtocols_excludesSslV3() {
        assertThat(NginxTlsConfig.NGINX_SSL_PROTOCOLS).doesNotContain("SSLv3");
    }

    @Test
    void nginxProtocols_excludesTls10() {
        assertThat(NginxTlsConfig.NGINX_SSL_PROTOCOLS).doesNotContain("TLSv1.0");
    }

    @Test
    void nginxProtocols_excludesTls11() {
        assertThat(NginxTlsConfig.NGINX_SSL_PROTOCOLS).doesNotContain("TLSv1.1");
    }

    // ── Approved cipher suites (V12.1.2) ─────────────────────────────────────

    @Test
    void nginxCiphers_allUseEcdheForwardSecrecy() {
        for (String cipher : NginxTlsConfig.NGINX_SSL_CIPHERS.split(":")) {
            assertThat(cipher).as("Cipher %s must use ECDHE for forward secrecy", cipher)
                    .containsIgnoringCase("ECDHE");
        }
    }

    @Test
    void nginxCiphers_allUseGcmAead() {
        for (String cipher : NginxTlsConfig.NGINX_SSL_CIPHERS.split(":")) {
            assertThat(cipher).as("Cipher %s must use GCM (AEAD mode)", cipher)
                    .containsIgnoringCase("GCM");
        }
    }

    @Test
    void nginxCiphers_includesEcdsaVariants() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).contains("ECDSA");
    }

    @Test
    void nginxCiphers_includesRsaVariants() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).contains("RSA");
    }

    @Test
    void nginxCiphers_includesAes256Suite() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).contains("AES256");
    }

    @Test
    void nginxCiphers_includesAes128Suite() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).contains("AES128");
    }

    @Test
    void nginxCiphers_atLeastFourSuites() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS.split(":").length)
                .as("At least 4 cipher suites must be defined")
                .isGreaterThanOrEqualTo(4);
    }

    // ── Forbidden cipher patterns (V12.1.2) ───────────────────────────────────

    @Test
    void nginxCiphers_doNotContainForbiddenPatterns() {
        for (String forbidden : NginxTlsConfig.NGINX_FORBIDDEN_CIPHER_PATTERNS) {
            assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS)
                    .as("Cipher string must not contain forbidden pattern: %s", forbidden)
                    .doesNotContainIgnoringCase(forbidden);
        }
    }

    @Test
    void nginxCiphers_doNotContainRc4() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).doesNotContainIgnoringCase("RC4");
    }

    @Test
    void nginxCiphers_doNotContain3des() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).doesNotContainIgnoringCase("3DES");
    }

    @Test
    void nginxCiphers_doNotContainNull() {
        assertThat(NginxTlsConfig.NGINX_SSL_CIPHERS).doesNotContainIgnoringCase("NULL");
    }

    // ── Ports (V12.2.1) ───────────────────────────────────────────────────────

    @Test
    void httpsPort_is443() {
        assertThat(NginxTlsConfig.HTTPS_PORT).isEqualTo(443);
    }

    @Test
    void httpRedirectPort_is80() {
        assertThat(NginxTlsConfig.HTTP_REDIRECT_PORT).isEqualTo(80);
    }

    @Test
    void httpAndHttpsPorts_areDifferent() {
        assertThat(NginxTlsConfig.HTTP_REDIRECT_PORT)
                .isNotEqualTo(NginxTlsConfig.HTTPS_PORT);
    }
}
