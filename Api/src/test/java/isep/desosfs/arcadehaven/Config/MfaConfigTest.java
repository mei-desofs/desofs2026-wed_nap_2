package isep.desosfs.arcadehaven.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that keycloak/realm-export.json is configured with:
 *   - TOTP OTP policy
 *   - Browser flow with Conditional OTP that gates on the ADMIN realm role
 *   - Audience protocol mapper on the arcadehaven-public client
 *
 * Note: TOTP is enforced at the browser (interactive) flow level, not at the
 * direct-grant level, so that programmatic/API access via Postman or CI remains
 * unaffected. This is the standard OIDC MFA enforcement pattern.
 */
class MfaConfigTest {

    private static JsonNode realm;

    @BeforeAll
    static void loadRealm() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        realm = mapper.readTree(Paths.get("../keycloak/realm-export.json").toFile());
    }

    // ── OTP policy ──────────────────────────────────────────────────────

    @Test
    void otpPolicy_isTotpType() {
        assertThat(realm.path("otpPolicyType").asText())
                .as("OTP policy must be TOTP (time-based)")
                .isEqualTo("totp");
    }

    @Test
    void otpPolicy_uses6Digits() {
        assertThat(realm.path("otpPolicyDigits").asInt())
                .as("TOTP must use 6-digit codes")
                .isEqualTo(6);
    }

    @Test
    void otpPolicy_period_is30Seconds() {
        assertThat(realm.path("otpPolicyPeriod").asInt())
                .as("TOTP period must be 30 s")
                .isEqualTo(30);
    }

    @Test
    void otpPolicy_codesAreNotReusable() {
        assertThat(realm.path("otpPolicyCodeReusable").asBoolean())
                .as("TOTP codes must not be reusable")
                .isFalse();
    }

    // ── Admin user exists and has ADMIN role ─────────────

    @Test
    void adminUser_exists_withAdminRole() {
        JsonNode adminUser = findUser("admin");
        assertThat(adminUser).as("admin user must exist in realm-export.json").isNotNull();

        List<String> realmRoles = new ArrayList<>();
        for (JsonNode role : adminUser.path("realmRoles")) {
            realmRoles.add(role.asText());
        }
        assertThat(realmRoles)
                .as("admin user must carry the ADMIN realm role (required for TOTP condition)")
                .contains("ADMIN");
    }

    // ── Authentication flows with conditional OTP ───────────────────────

    @Test
    void authFlows_containConditionalOtpFlow() {
        boolean hasConditionalOtp = false;
        for (JsonNode flow : realm.path("authenticationFlows")) {
            if (flow.path("alias").asText().contains("Conditional OTP")) {
                hasConditionalOtp = true;
                break;
            }
        }
        assertThat(hasConditionalOtp)
                .as("realm must define a Conditional OTP authentication flow")
                .isTrue();
    }

    @Test
    void authFlows_conditionalOtpUsesRoleConditionAuthenticator() {
        boolean hasRoleCondition = false;
        for (JsonNode flow : realm.path("authenticationFlows")) {
            for (JsonNode exec : flow.path("authenticationExecutions")) {
                if ("conditional-user-role".equals(exec.path("authenticator").asText())) {
                    hasRoleCondition = true;
                    break;
                }
            }
        }
        assertThat(hasRoleCondition)
                .as("a conditional OTP flow must check user role via conditional-user-role")
                .isTrue();
    }

    @Test
    void browserFlow_isBoundToCustomBrowserAlias() {
        assertThat(realm.path("browserFlow").asText())
                .as("realm must bind browserFlow to the custom 'browser' flow")
                .isEqualTo("browser");
    }

    @Test
    void directGrantFlow_isBoundToCustomAlias() {
        assertThat(realm.path("directGrantFlow").asText())
                .as("Direct grant must use the custom flow that explicitly skips OTP for API compatibility")
                .isEqualTo("arcadehaven-direct-grant");
    }

    @Test
    void directGrantFlow_doesNotContainOtpStep() {
        JsonNode customDirectGrant = null;
        for (JsonNode flow : realm.path("authenticationFlows")) {
            if ("arcadehaven-direct-grant".equals(flow.path("alias").asText())) {
                customDirectGrant = flow;
                break;
            }
        }
        assertThat(customDirectGrant).as("arcadehaven-direct-grant flow must exist").isNotNull();

        for (JsonNode exec : customDirectGrant.path("authenticationExecutions")) {
            String authenticator = exec.path("authenticator").asText("");
            assertThat(authenticator)
                    .as("Direct grant flow must not contain any OTP authenticator step")
                    .doesNotContain("otp");
        }
    }

    // ── Authenticator config targets ADMIN role ─────────────────────────

    @Test
    void authenticatorConfig_definesAdminRoleCondition() {
        boolean hasAdminRoleConfig = false;
        for (JsonNode cfg : realm.path("authenticatorConfig")) {
            if ("ADMIN".equals(cfg.path("config").path("condUserCurrentRole").asText())) {
                hasAdminRoleConfig = true;
                break;
            }
        }
        assertThat(hasAdminRoleConfig)
                .as("authenticator config must set condUserCurrentRole=ADMIN")
                .isTrue();
    }

    // ── Audience protocol mapper on the public client ───────────────────

    @Test
    void client_arcadehavenPublic_hasAudienceMapper() {
        JsonNode client = findClient("arcadehaven-public");
        assertThat(client).as("arcadehaven-public client must exist").isNotNull();

        boolean hasAudienceMapper = false;
        for (JsonNode mapper : client.path("protocolMappers")) {
            if ("oidc-audience-mapper".equals(mapper.path("protocolMapper").asText())) {
                hasAudienceMapper = true;
                break;
            }
        }
        assertThat(hasAudienceMapper)
                .as("arcadehaven-public must have an oidc-audience-mapper to set aud claim")
                .isTrue();
    }

    @Test
    void audienceMapper_addsArcadehavenApiAudience() {
        JsonNode client = findClient("arcadehaven-public");
        assertThat(client).isNotNull();

        String includedAudience = null;
        for (JsonNode mapper : client.path("protocolMappers")) {
            if ("oidc-audience-mapper".equals(mapper.path("protocolMapper").asText())) {
                includedAudience = mapper.path("config").path("included.custom.audience").asText();
                break;
            }
        }
        assertThat(includedAudience)
                .as("audience mapper must add 'arcadehaven-api' to the aud claim")
                .isEqualTo("arcadehaven-api");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private JsonNode findUser(String username) {
        for (JsonNode u : realm.path("users")) {
            if (username.equals(u.path("username").asText())) return u;
        }
        return null;
    }

    private JsonNode findClient(String clientId) {
        for (JsonNode c : realm.path("clients")) {
            if (clientId.equals(c.path("clientId").asText())) return c;
        }
        return null;
    }
}
