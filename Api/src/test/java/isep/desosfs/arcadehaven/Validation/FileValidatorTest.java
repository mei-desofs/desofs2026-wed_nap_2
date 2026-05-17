package isep.desosfs.arcadehaven.Validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;

class FileValidatorTest {

    private final FileValidator validator = new FileValidator();

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0, 0, 0, 0x0D, 0x49, 0x48, 0x44, 0x52
    };

    @Test
    void shouldAcceptPngForImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("f", "img.png", "image/png", PNG_BYTES);
        assertDoesNotThrow(() -> validator.validateMimeType(file, FileType.IMAGE));
    }

    @Test
    void shouldAcceptPngForScreenshot() throws Exception {
        MockMultipartFile file = new MockMultipartFile("f", "shot.png", "image/png", PNG_BYTES);
        assertDoesNotThrow(() -> validator.validateMimeType(file, FileType.SCREENSHOT));
    }

    @Test
    void shouldAcceptPngForCover() throws Exception {
        MockMultipartFile file = new MockMultipartFile("f", "cover.png", "image/png", PNG_BYTES);
        assertDoesNotThrow(() -> validator.validateMimeType(file, FileType.COVER));
    }

    @Test
    void shouldRejectPdfForImage() {
        MockMultipartFile file = new MockMultipartFile("f", "doc.pdf", "application/pdf",
                "%PDF-1.4 content here".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateMimeType(file, FileType.IMAGE));
    }

    @Test
    void shouldRejectPdfForScreenshot() {
        MockMultipartFile file = new MockMultipartFile("f", "doc.pdf", "application/pdf",
                "%PDF-1.4 content here".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateMimeType(file, FileType.SCREENSHOT));
    }

    @Test
    void shouldRejectPdfForCover() {
        MockMultipartFile file = new MockMultipartFile("f", "doc.pdf", "application/pdf",
                "%PDF-1.4 content here".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateMimeType(file, FileType.COVER));
    }

    @Test
    void shouldRejectTextFileForImage() {
        MockMultipartFile file = new MockMultipartFile("f", "plain.txt", "text/plain",
                "just some text content".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateMimeType(file, FileType.IMAGE));
    }
}
