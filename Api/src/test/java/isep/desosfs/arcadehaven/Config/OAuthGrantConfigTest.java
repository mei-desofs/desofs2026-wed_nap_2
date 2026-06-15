package isep.desosfs.arcadehaven.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that keycloak/realm-export.json is configured with the correct
 * OAuth 2.0 grant types and that all compensating controls for the retained
 * Direct Access Grant (ROPC) are in place.
 *
 * ASVS V10.4.4 requires only Authorization Code grant with PKCE. ArcadeHaven
 * retains the Direct Access Grant for the current API-only architecture
 * (see Documentation/SecurityDeviations.md — V10.4.4). The tests below verify:
 *   1. The more dangerous Implicit Flow is disabled.
 *   2. PKCE S256 is configured for future Authorization Code flows.
 *   3. The Direct Access Grant retention is formally documented in the client description.
 *   4. Compensating controls (brute-force protection, single-use refresh tokens,
 *      short access token lifetime) are active.
 */
class OAuthGrantConfigTest {

    private static JsonNode realm;

    @BeforeAll
    static void loadRealm() throws IOException {
        realm = new ObjectMapper()
                .readTree(Paths.get("../keycloak/realm-export.json").toFile());
    }

    private static JsonNode publicClient() {
        for (JsonNode c : realm.path("clients")) {
            if ("arcadehaven-public".equals(c.path("clientId").asText())) {
                return c;
            }
        }
        throw new AssertionError("arcadehaven-public client not found in realm-export.json");
    }

    // ── V10.4.4 — grant type configuration ──────────────────────────────────

    @Test
    void implicitFlow_isDisabled() {
        assertThat(publicClient().path("implicitFlowEnabled").asBoolean())
                .as("Implicit Flow must be disabled (V10.4.4) — most dangerous grant type")
                .isFalse();
    }

    @Test
    void pkce_isConfiguredWithS256() {
        String method = publicClient()
                .path("attributes")
                .path("pkce.code.challenge.method")
                .asText("");
        assertThat(method)
                .as("PKCE challenge method must be S256 for future Authorization Code flows (V10.4.4)")
                .isEqualTo("S256");
    }

    @Test
    void directAccessGrant_hasDocumentedRationale_inClientDescription() {
        String description = publicClient().path("description").asText("");
        assertThat(description)
                .as("Direct Access Grant retention must have a documented rationale in the client description")
                .containsIgnoringCase("Direct Access Grant");
    }

    // ── Compensating controls for retained Direct Access Grant ───────────────

    @Test
    void bruteForceProtection_isEnabled() {
        assertThat(realm.path("bruteForceProtected").asBoolean())
                .as("Keycloak brute-force protection must be enabled as compensating control for ROPC")
                .isTrue();
    }

    @Test
    void refreshToken_isSingleUse() {
        assertThat(realm.path("revokeRefreshToken").asBoolean())
                .as("Refresh tokens must be single-use (revokeRefreshToken=true) to limit ROPC session risk")
                .isTrue();
    }

    @Test
    void accessToken_lifetime_isAtMost300Seconds() {
        int lifetime = realm.path("accessTokenLifespan").asInt();
        assertThat(lifetime)
                .as("Access token lifetime must be short (≤300 s) to minimise ROPC exposure window")
                .isGreaterThan(0)
                .isLessThanOrEqualTo(300);
    }
}
