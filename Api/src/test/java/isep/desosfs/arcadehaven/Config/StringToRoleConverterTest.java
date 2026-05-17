package isep.desosfs.arcadehaven.Config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.Role;

class StringToRoleConverterTest {

    private final StringToRoleConverter converter = new StringToRoleConverter();

    @Test
    void shouldConvertAdmin() {
        assertEquals(Role.ADMIN, converter.convert("ADMIN"));
    }

    @Test
    void shouldConvertPublisher() {
        assertEquals(Role.PUBLISHER, converter.convert("PUBLISHER"));
    }

    @Test
    void shouldConvertBuyer() {
        assertEquals(Role.BUYER, converter.convert("BUYER"));
    }

    @Test
    void shouldConvertLowercase() {
        assertEquals(Role.BUYER, converter.convert("buyer"));
    }

    @Test
    void shouldConvertMixedCase() {
        assertEquals(Role.ADMIN, converter.convert("Admin"));
    }

    @Test
    void shouldConvertWithLeadingTrailingWhitespace() {
        assertEquals(Role.PUBLISHER, converter.convert("  PUBLISHER  "));
    }

    @Test
    void shouldThrowForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert("SUPERUSER"));
    }
}
