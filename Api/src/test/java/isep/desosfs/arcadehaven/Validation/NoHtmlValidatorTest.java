package isep.desosfs.arcadehaven.Validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NoHtmlValidatorTest {

    private final NoHtmlValidator validator = new NoHtmlValidator();

    @Test
    void shouldReturnTrueForPlainText() {
        assertTrue(validator.isValid("Hello World", null));
    }

    @Test
    void shouldReturnTrueForNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldReturnTrueForEmptyString() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    void shouldReturnFalseForScriptTag() {
        assertFalse(validator.isValid("<script>alert(1)</script>", null));
    }

    @Test
    void shouldReturnFalseForBoldTag() {
        assertFalse(validator.isValid("<b>bold</b>", null));
    }

    @Test
    void shouldReturnFalseForAnchorTag() {
        assertFalse(validator.isValid("<a href='url'>link</a>", null));
    }

    @Test
    void shouldReturnFalseForSelfClosingTag() {
        assertFalse(validator.isValid("<br/>", null));
    }

    @Test
    void shouldReturnTrueForLessThanWithoutClosingAngle() {
        assertTrue(validator.isValid("5 < 10", null));
    }

    @Test
    void shouldReturnFalseForImageTag() {
        assertFalse(validator.isValid("<img src='x' onerror='alert(1)'>", null));
    }

    @Test
    void shouldReturnTrueForUrlWithSpecialChars() {
        assertTrue(validator.isValid("Visit https://example.com?a=1&b=2", null));
    }
}
