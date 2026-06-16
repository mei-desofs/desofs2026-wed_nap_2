package isep.desosfs.arcadehaven.Validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;

class FileValidatorTest {

    private final FileValidator validator = new FileValidator();

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0, 0, 0, 0x0D, 0x49, 0x48, 0x44, 0x52
    };

    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0, 0x10, 0x4A, 0x46, 0x49, 0x46, 0, 0x01
    };

    // ── init() ───────────────────────────────────────────────────────────────────

    @Test
    void init_parsesMaxFileSizeProperly() {
        ReflectionTestUtils.setField(validator, "maxFileSize", "10MB");
        validator.init();
        long maxBytes = (long) ReflectionTestUtils.getField(validator, "maxFileSizeBytes");
        assertEquals(10 * 1024 * 1024L, maxBytes);
    }

    // ── validateFileSize() ────────────────────────────────────────────────────────

    @Test
    void validateFileSize_underLimit_doesNotThrow() {
        ReflectionTestUtils.setField(validator, "maxFileSizeBytes", 1000L);
        MockMultipartFile file = new MockMultipartFile("f", "img.png", "image/png", new byte[100]);
        assertDoesNotThrow(() -> validator.validateFileSize(file));
    }

    @Test
    void validateFileSize_atLimit_doesNotThrow() {
        ReflectionTestUtils.setField(validator, "maxFileSizeBytes", 1000L);
        MockMultipartFile file = new MockMultipartFile("f", "img.png", "image/png", new byte[1000]);
        assertDoesNotThrow(() -> validator.validateFileSize(file));
    }

    @Test
    void validateFileSize_overLimit_throwsMaxUploadSizeExceeded() {
        ReflectionTestUtils.setField(validator, "maxFileSizeBytes", 1000L);
        MockMultipartFile file = new MockMultipartFile("f", "img.png", "image/png", new byte[1001]);
        assertThrows(MaxUploadSizeExceededException.class, () -> validator.validateFileSize(file));
    }

    // ── validateExtension() ────────────────────────────────────────────────────────

    @Test
    void validateExtension_validPng_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile("f", "img.png", "image/png", PNG_BYTES);
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.IMAGE));
    }

    @Test
    void validateExtension_validJpeg_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile("f", "img.jpeg", "image/jpeg", JPEG_BYTES);
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.IMAGE));
    }

    @Test
    void validateExtension_validJpg_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile("f", "img.jpg", "image/jpeg", JPEG_BYTES);
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.IMAGE));
    }

    @Test
    void validateExtension_validWebp_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile("f", "img.webp", "image/webp",
                new byte[]{0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50});
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.SCREENSHOT));
    }

    @Test
    void validateExtension_coverType_validPng_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile("f", "cover.png", "image/png", PNG_BYTES);
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.COVER));
    }

    @Test
    void validateExtension_screenshotType_validJpeg_doesNotThrow() {
        MockMultipartFile file = new MockMultipartFile("f", "shot.jpeg", "image/jpeg", JPEG_BYTES);
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.SCREENSHOT));
    }

    @Test
    void validateExtension_pdfExtension_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile("f", "doc.pdf", "application/pdf",
                new byte[]{0x25, 0x50, 0x44, 0x46});
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateExtension(file, FileType.IMAGE));
    }

    @Test
    void validateExtension_exeExtension_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile("f", "malware.exe", "application/octet-stream",
                new byte[]{0x4D, 0x5A});
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateExtension(file, FileType.IMAGE));
        assertTrue(ex.getMessage().contains("Unsupported image extension"));
    }

    @Test
    void validateExtension_noExtension_throwsMissingExtension() {
        MockMultipartFile file = new MockMultipartFile("f", "noextension", "image/png", PNG_BYTES);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateExtension(file, FileType.IMAGE));
        assertTrue(ex.getMessage().contains("Missing file extension"));
    }

    @Test
    void validateExtension_dotAtEnd_throwsMissingExtension() {
        MockMultipartFile file = new MockMultipartFile("f", "filename.", "image/png", PNG_BYTES);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateExtension(file, FileType.IMAGE));
        assertTrue(ex.getMessage().contains("Missing file extension"));
    }

    @Test
    void validateExtension_nullFilename_throwsMissingFileName() {
        MockMultipartFile file = new MockMultipartFile("f", null, "image/png", PNG_BYTES);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateExtension(file, FileType.IMAGE));
        assertTrue(ex.getMessage().contains("Missing file name"));
    }

    @Test
    void validateExtension_windowsPath_extractsCorrectExtension() {
        MockMultipartFile file = new MockMultipartFile("f",
                "C:\\Users\\user\\images\\photo.png", "image/png", PNG_BYTES);
        assertDoesNotThrow(() -> validator.validateExtension(file, FileType.IMAGE));
    }

    // ── validateMimeType() (existing + error path) ────────────────────────────────

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
