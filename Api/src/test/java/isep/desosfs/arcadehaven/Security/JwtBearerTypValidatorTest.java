package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtBearerTypValidator (ASVS V9.2.2).
 * Verifies that only Bearer access tokens are accepted; ID tokens and other types are rejected.
 */
class JwtBearerTypValidatorTest {

    private final JwtBearerTypValidator validator = new JwtBearerTypValidator();

    private Jwt buildJwt(String typ) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("sub", "user-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        if (typ != null) {
            builder.claim("typ", typ);
        }
        return builder.build();
    }

    @Test
    void bearerToken_passes() {
        OAuth2TokenValidatorResult result = validator.validate(buildJwt("Bearer"));
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void bearerToken_caseInsensitive_passes() {
        assertThat(validator.validate(buildJwt("bearer")).hasErrors()).isFalse();
        assertThat(validator.validate(buildJwt("BEARER")).hasErrors()).isFalse();
    }

    @Test
    void missingTypClaim_passes() {
        // Keycloak may omit typ on some versions — fail-open is intentional
        OAuth2TokenValidatorResult result = validator.validate(buildJwt(null));
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void idToken_rejected() {
        OAuth2TokenValidatorResult result = validator.validate(buildJwt("ID"));
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).anyMatch(e ->
                e.getErrorCode().equals("invalid_token") &&
                e.getDescription().contains("Bearer"));
    }

    @Test
    void refreshToken_rejected() {
        OAuth2TokenValidatorResult result = validator.validate(buildJwt("Refresh"));
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void arbitraryType_rejected() {
        OAuth2TokenValidatorResult result = validator.validate(buildJwt("SomeOtherType"));
        assertThat(result.hasErrors()).isTrue();
    }
}
