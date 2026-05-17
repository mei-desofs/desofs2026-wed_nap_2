package isep.desosfs.arcadehaven.Security;

import org.springframework.context.annotation.Configuration;

/**
 * ASVS V12.1.1 / V12.1.2 — TLS protocol and cipher suite policy.
 *
 * Protocol restrictions (V12.1.1) are enforced via application.properties:
 *   server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
 *
 * Cipher suite restrictions (V12.1.2) are enforced via:
 *   server.ssl.ciphers=<APPROVED_CIPHER_SUITES_CSV>
 * (active only when server.ssl.enabled=true)
 *
 * When TLS is terminated by a reverse proxy (current production topology),
 * apply equivalent restrictions at the nginx layer:
 *   ssl_protocols TLSv1.2 TLSv1.3;
 *   ssl_ciphers ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:...;
 */
@Configuration
public class WebServerTlsConfig {

    /** Allowed TLS protocols — TLS 1.0 and 1.1 are explicitly excluded. */
    static final String ENABLED_TLS_PROTOCOLS = "TLSv1.2,TLSv1.3";

    /** Protocols that must never be enabled in any environment. */
    static final String[] FORBIDDEN_TLS_PROTOCOLS = {"SSLv2", "SSLv3", "TLSv1.0", "TLSv1.1"};

    /**
     * ASVS V12.1.2 — Forward-secrecy (ECDHE) cipher suites only; RC4, 3DES, and CBC-mode
     * suites without AEAD are excluded. TLS 1.3 cipher suites are managed by the JVM and
     * do not need to be listed here.
     */
    static final String[] APPROVED_CIPHER_SUITES = {
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
    };

    /** Cipher suites that are explicitly prohibited (no forward secrecy or broken primitives). */
    static final String[] FORBIDDEN_CIPHER_SUITES = {
        "TLS_RSA_WITH_AES_256_CBC_SHA256",   // no forward secrecy
        "TLS_RSA_WITH_AES_128_CBC_SHA",      // no forward secrecy
        "SSL_RSA_WITH_RC4_128_SHA",          // RC4 broken
        "TLS_RSA_WITH_3DES_EDE_CBC_SHA"     // 3DES / SWEET32
    };
}
