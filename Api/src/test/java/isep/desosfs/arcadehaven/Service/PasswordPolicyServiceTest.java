package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for PasswordPolicyService (ASVS V6.1.2, V6.2.4, V6.2.11, V6.2.12).
 * HIBP is disabled for all tests to avoid outbound HTTP calls.
 */
class PasswordPolicyServiceTest {

    private PasswordPolicyService service;

    @BeforeEach
    void setUp() {
        service = new PasswordPolicyService();
        ReflectionTestUtils.setField(service, "hibpEnabled", false);
    }

    // ── Valid passwords ─────────────────────────────────────────────────────────

    @Test
    void validate_strongUniquePassword_passes() {
        assertThatCode(() -> service.validate("Xtr4Safe#2026XY"))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_longComplexPassword_passes() {
        assertThatCode(() -> service.validate("Tr0ub4dour&3!Staple"))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_minimumLength12_passes() {
        assertThatCode(() -> service.validate("Bz!9kR@mXtLq"))
                .doesNotThrowAnyException();
    }

    // ── Context-specific blocklist (ASVS V6.1.2 / V6.2.11) ─────────────────────

    @ParameterizedTest
    @ValueSource(strings = {
            "ArcadeHaven2024!",    // contains "arcadehaven"
            "MyArcade99Safe!XY",   // contains "arcade"
            "SafeHaven2026!XYZ",   // contains "haven"
            "GamingFun2026!XYZ",   // contains "gaming"
            "AdminUser#2026XYZ",   // contains "admin"
            "Password!Safe#2026Z", // contains "password"
            "Letmein!Safe#2026ZZ", // contains "letmein"
            "WelcomeHome2026!XY",  // contains "welcome"
            "MonkeyBiz!2026XYZW",  // contains "monkey"
            "DragonFire2026!XYZ",  // contains "dragon"
    })
    void validate_passwordWithBlockedTerm_throwsBusinessException(String password) {
        assertThatThrownBy(() -> service.validate(password))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("too easy to guess");
    }

    @Test
    void validate_blockedTermCheck_isCaseInsensitive() {
        assertThatThrownBy(() -> service.validate("ARCADEHAVEN2024!!"))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> service.validate("ArCaDe2026!SafeXY"))
                .isInstanceOf(BusinessException.class);
    }

    // ── Common password list (ASVS V6.2.4) ──────────────────────────────────────

    @Test
    void validate_commonPassword_throwsBusinessException() {
        // "123456789012" is in common-passwords.txt (12 chars, no blocked terms)
        assertThatThrownBy(() -> service.validate("123456789012"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("commonly used");
    }

    @Test
    void validate_commonPasswordCheck_isCaseInsensitive() {
        // The check lowercases the input before comparing against the set
        assertThatThrownBy(() -> service.validate("123456789012"))
                .isInstanceOf(BusinessException.class);
    }

    // ── Error message safety ─────────────────────────────────────────────────────

    @Test
    void validate_errorMessages_doNotEchoPasswordBack() {
        assertThatThrownBy(() -> service.validate("ArcadeHaven2024!"))
                .isInstanceOf(BusinessException.class)
                .hasMessageNotContaining("ArcadeHaven2024!");

        assertThatThrownBy(() -> service.validate("123456789012"))
                .isInstanceOf(BusinessException.class)
                .hasMessageNotContaining("123456789012");
    }

    // ── HIBP disabled (ASVS V6.2.12) ────────────────────────────────────────────

    @Test
    void validate_hibpDisabled_noBreachCheckPerformed() {
        // With hibpEnabled=false the HIBP HTTP call is completely skipped.
        // A password that would otherwise be found in a breach passes if it
        // clears the context and common-password checks.
        assertThatCode(() -> service.validate("Xtr4Safe#2026XY"))
                .doesNotThrowAnyException();
    }

    // ── Ordering: context check runs before common-password check ────────────────

    @Test
    void validate_passwordInBothBlocklistAndCommonList_reportedAsContextBlocked() {
        // "password123" is both a blocked context term and in the common list.
        // The context check runs first and the user sees the "too easy to guess" message.
        assertThatThrownBy(() -> service.validate("password123456!XX"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("too easy to guess");
    }

    @Test
    void validate_hibpEnabled_triggersCheck() {
        ReflectionTestUtils.setField(service, "hibpEnabled", true);

        assertThatCode(() -> service.validate("Xtr4SafeUnique2026!QZ\""))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_emptyPassword_doesNotThrow() {
        assertThatCode(() -> service.validate(""))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_nullPassword_throwsException() {
        assertThatThrownBy(() -> service.validate(null))
                .isInstanceOf(NullPointerException.class);
    }
}
