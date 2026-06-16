package isep.desosfs.arcadehaven.Config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that application.properties configures JWT audience and issuer
 * validation to prevent audience confusion attacks (ASVS V9.2.3 / V9.2.4).
 */
class JwtAudienceConfigTest {

    private static Properties props;

    @BeforeAll
    static void loadProperties() throws IOException {
        props = new Properties();
        try (InputStream in = JwtAudienceConfigTest.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            assertThat(in).as("application.properties must be on classpath").isNotNull();
            props.load(in);
        }
    }

    @Test
    void jwtAudiences_isConfigured_asvs_v9_2_3() {
        String audiences = props.getProperty("spring.security.oauth2.resourceserver.jwt.audiences");
        assertThat(audiences)
                .as("jwt.audiences must be set to enforce aud claim validation (ASVS V9.2.3)")
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void jwtAudiences_containsArcadehavenApi() {
        String audiences = props.getProperty("spring.security.oauth2.resourceserver.jwt.audiences");
        assertThat(audiences)
                .as("jwt.audiences must be 'arcadehaven-api' to bind tokens to this service (ASVS V9.2.4)")
                .isEqualTo("arcadehaven-api");
    }

    @Test
    void jwkSetUri_isConfigured() {
        String jwkSetUri = props.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        assertThat(jwkSetUri)
                .as("jwk-set-uri must be configured for JWT signature validation (V9.1.3)")
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void issuerUri_isConfigured() {
        String issuerUri = props.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        assertThat(issuerUri)
                .as("issuer-uri must be set to validate iss claim (ASVS V10.5.3)")
                .isNotNull()
                .contains("realms/arcadehaven");
    }
}
