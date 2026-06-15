package isep.desosfs.arcadehaven.Security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * ASVS V9.2.2 — Validates that the JWT typ claim is "Bearer", preventing
 * ID tokens or refresh tokens from being used as access tokens.
 * Tokens without a typ claim are accepted (Keycloak may omit it on older versions).
 */
public class JwtBearerTypValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TYP = new OAuth2Error(
            "invalid_token",
            "Token type (typ) must be 'Bearer'; ID tokens and other types are not accepted as access tokens",
            null);

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String typ = token.getClaimAsString("typ");
        if (typ != null && !"Bearer".equalsIgnoreCase(typ)) {
            return OAuth2TokenValidatorResult.failure(INVALID_TYP);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
