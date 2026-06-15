package isep.desosfs.arcadehaven.Security;

import org.springframework.context.annotation.Configuration;

/**
 * ASVS V12.1.1 / V12.1.2 / V12.2.1 — nginx reverse-proxy TLS policy.
 *
 * These constants define the expected values in Api/nginx/nginx.conf and are
 * validated by NginxTlsConfigTest. Keeping the policy in Java makes it
 * independently testable without running nginx.
 *
 * nginx.conf directives that correspond to each constant:
 *   ssl_protocols  → NGINX_SSL_PROTOCOLS
 *   ssl_ciphers    → NGINX_SSL_CIPHERS
 *   listen 443 ssl → HTTPS_PORT
 *   listen 80      → HTTP_REDIRECT_PORT (HTTP → HTTPS redirect)
 */
@Configuration
public class NginxTlsConfig {

    /** V12.1.1 — value of the nginx ssl_protocols directive */
    static final String NGINX_SSL_PROTOCOLS = "TLSv1.2 TLSv1.3";

    /**
     * V12.1.2 — value of the nginx ssl_ciphers directive (OpenSSL naming).
     * Only ECDHE suites with AES-GCM are listed; forward secrecy and AEAD
     * are mandatory. RC4, 3DES, and CBC-without-AEAD suites are absent.
     */
    static final String NGINX_SSL_CIPHERS =
            "ECDHE-ECDSA-AES256-GCM-SHA384:" +
            "ECDHE-RSA-AES256-GCM-SHA384:"   +
            "ECDHE-ECDSA-AES128-GCM-SHA256:" +
            "ECDHE-RSA-AES128-GCM-SHA256";

    /** Cipher name fragments that must NOT appear in the nginx cipher string */
    static final String[] NGINX_FORBIDDEN_CIPHER_PATTERNS = {
        "RC4",   // broken stream cipher
        "3DES",  // SWEET32 / birthday attack
        "NULL",  // no encryption
        "MD5"    // broken hash
    };

    /** V12.2.1 — nginx listens here for TLS; all API clients must use this port */
    static final int HTTPS_PORT = 443;

    /** V12.2.1 — nginx listens here only to redirect to HTTPS; no plaintext API */
    static final int HTTP_REDIRECT_PORT = 80;
}
