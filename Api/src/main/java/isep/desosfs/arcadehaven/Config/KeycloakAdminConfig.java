package isep.desosfs.arcadehaven.Config;

import jakarta.annotation.PostConstruct;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Configuration
public class KeycloakAdminConfig {

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
        return new RestTemplate();
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
