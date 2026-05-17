package isep.desosfs.arcadehaven.Config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;

class StringToFileTypeConverterTest {

    private final StringToFileTypeConverter converter = new StringToFileTypeConverter();

    @Test
    void shouldConvertImage() {
        assertEquals(FileType.IMAGE, converter.convert("IMAGE"));
    }

    @Test
    void shouldConvertScreenshot() {
        assertEquals(FileType.SCREENSHOT, converter.convert("SCREENSHOT"));
    }

    @Test
    void shouldConvertCover() {
        assertEquals(FileType.COVER, converter.convert("COVER"));
    }

    @Test
    void shouldConvertLowercase() {
        assertEquals(FileType.IMAGE, converter.convert("image"));
    }

    @Test
    void shouldConvertMixedCase() {
        assertEquals(FileType.COVER, converter.convert("Cover"));
    }

    @Test
    void shouldConvertWithLeadingTrailingWhitespace() {
        assertEquals(FileType.IMAGE, converter.convert("  IMAGE  "));
    }

    @Test
    void shouldThrowForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert("INVALID"));
    }
}
