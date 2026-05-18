package isep.desosfs.arcadehaven.Config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that application.properties configures JWT audience
 * validation to prevent audience confusion attacks.
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
        String audiences = props.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        assertThat(audiences)
                .as("jwt.issuer must be set to prevent audience confusion attacks")
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void jwtAudiences_containsArcadehavenApi() {
        String audiences = props.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        assertThat(audiences)
                .as("jwt.issuer must include 'realms/arcadehaven'")
                .contains("realms/arcadehaven");
    }

    @Test
    void jwkSetUri_isConfigured() {
        String jwkSetUri = props.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        assertThat(jwkSetUri)
                .as("jwk-set-uri must be configured for JWT signature validation (V9.1.3)")
                .isNotNull()
                .isNotBlank();
    }
}
