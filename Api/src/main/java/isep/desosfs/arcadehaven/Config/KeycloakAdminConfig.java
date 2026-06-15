package isep.desosfs.arcadehaven.Config;

import jakarta.annotation.PostConstruct;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

@Configuration
public class KeycloakAdminConfig {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminConfig.class);

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    @Bean
    public RestTemplate restTemplate() {
        // V15.3.2 — outbound HTTP calls must not follow redirects automatically
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        return new RestTemplate(factory);
    }

    // ASVS V1.2.2
    @PostConstruct
    private void validateServerUrl() {
        URI uri = URI.create(serverUrl);

        if (uri.getScheme() == null) {
            throw new IllegalStateException("Keycloak server URL missing scheme");
        }

        List<String> allowedSchemes = List.of("http", "https");

        if (!allowedSchemes.contains(uri.getScheme().toLowerCase())) {
            throw new IllegalStateException("Invalid Keycloak URL scheme");
        }

        // V12.3.1 — warn when Keycloak is reached over plain HTTP.
        // HTTP is acceptable within the Docker-internal bridge network (traffic
        // does not leave the host), but HTTPS must be used for any external endpoint.
        if ("http".equalsIgnoreCase(uri.getScheme())) {
            log.warn("ASVS V12.3.1 — Keycloak server URL uses plain HTTP ({}). " +
                     "Acceptable only within a trusted Docker-internal network; " +
                     "configure HTTPS for any externally reachable Keycloak endpoint.", serverUrl);
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalStateException("Keycloak server URL missing host");
        }
    }

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .build();
    }
}
