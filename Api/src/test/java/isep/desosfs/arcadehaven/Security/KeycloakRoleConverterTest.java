package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KeycloakRoleConverterTest {
    private KeycloakRoleConverter converter;

    @BeforeEach
    void setup() {
        converter = new KeycloakRoleConverter();
    }

    @Test
    void shouldConvertValidRoles() {
        Jwt jwt = createJwt(List.of("ADMIN", "BUYER"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(2, authorities.size());

        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUYER")));
    }

    @Test
    void shouldIgnoreInvalidRoles() {
        Jwt jwt = createJwt(List.of("ADMIN", "USER", "TEST"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(1, authorities.size());

        assertEquals(
                "ROLE_ADMIN",
                authorities.iterator().next().getAuthority()
        );
    }

    @Test
    void shouldReturnEmptyWhenRealmAccessMissing() {
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("alg", "none"),
            Map.of(
                    "sub", "123"
            )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenRolesMissing() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of(
                        "realm_access",
                        Map.of()
                )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    private Jwt createJwt(List<String> roles) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of(
                        "realm_access",
                        Map.of(
                                "roles",
                                roles
                        )
                )
        );
    }
}
