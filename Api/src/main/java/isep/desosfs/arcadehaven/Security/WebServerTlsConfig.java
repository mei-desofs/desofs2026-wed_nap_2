package isep.desosfs.arcadehaven.Security;

import org.springframework.context.annotation.Configuration;

/**
 * ASVS V12.1.1 — Restrict TLS to version 1.2 and 1.3 only.
 *
 * Protocol restrictions are enforced via application.properties:
 *   server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
 *
 * When TLS is terminated by a reverse proxy (current production topology),
 * equivalent restrictions must be applied at the proxy layer:
 *   nginx: ssl_protocols TLSv1.2 TLSv1.3;
 */
@Configuration
public class WebServerTlsConfig {

    /** Allowed TLS protocols — TLS 1.0 and 1.1 are explicitly excluded. */
    static final String ENABLED_TLS_PROTOCOLS = "TLSv1.2,TLSv1.3";

    /** Protocols that must never be enabled in any environment. */
    static final String[] FORBIDDEN_TLS_PROTOCOLS = {"SSLv2", "SSLv3", "TLSv1.0", "TLSv1.1"};
}
