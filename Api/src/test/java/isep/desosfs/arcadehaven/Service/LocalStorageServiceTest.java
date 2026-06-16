package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LocalStorageService — RF-20, RF-25, RF-31, RF-32.
 *
 * RF-31 / RF-32: verifies that required directories are created at startup
 * and that files are stored in the correct sub-directory.
 * RF-20 / RF-25: verifies that bytes can be written and read back correctly.
 */
class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalStorageService(tempDir.toString());
    }

    // ── RF-31 / RF-32 — startup directory creation ───────────────────────────────

    @Test
    void ensureBaseDirectories_createsInvoicesDir() throws IOException {
        service.ensureBaseDirectories();
        assertThat(tempDir.resolve("invoices")).isDirectory();
    }

    @Test
    void ensureBaseDirectories_createsKeysDir() throws IOException {
        service.ensureBaseDirectories();
        assertThat(tempDir.resolve("keys")).isDirectory();
    }

    @Test
    void ensureBaseDirectories_createsGamesImagesDir() throws IOException {
        service.ensureBaseDirectories();
        assertThat(tempDir.resolve("games/images")).isDirectory();
    }

    @Test
    void ensureBaseDirectories_isIdempotent() throws IOException {
        service.ensureBaseDirectories();
        service.ensureBaseDirectories(); // second call must not throw
        assertThat(tempDir.resolve("invoices")).isDirectory();
    }

    // ── RF-20 — invoice upload (uploadBytes) ─────────────────────────────────────

    @Test
    void uploadBytes_createsFileWithCorrectContent() {
        byte[] data = "INVOICE CONTENT".getBytes(StandardCharsets.UTF_8);
        String path = service.uploadBytes(data, "invoice_test.txt", "invoices");

        assertThat(tempDir.resolve(path)).exists();
        assertThat(tempDir.resolve(path)).hasContent("INVOICE CONTENT");
    }

    @Test
    void uploadBytes_returnsRelativePath() {
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        String path = service.uploadBytes(data, "test.txt", "invoices");

        assertThat(Path.of(path).isAbsolute()).isFalse();
    }

    @Test
    void uploadBytes_storesFileUnderCorrectSubdir() {
        byte[] data = "keys content".getBytes(StandardCharsets.UTF_8);
        String path = service.uploadBytes(data, "keys_order1.txt", "keys");

        assertThat(Path.of(path).getParent()).isEqualTo(Path.of("keys"));
    }

    // ── RF-25 — invoice download (downloadFile) ───────────────────────────────────

    @Test
    void downloadFile_returnsUploadedContent() {
        byte[] original = "Hello Invoice".getBytes(StandardCharsets.UTF_8);
        String path = service.uploadBytes(original, "dl_test.txt", "invoices");

        byte[] downloaded = service.downloadFile(path);

        assertThat(downloaded).isEqualTo(original);
    }

    @Test
    void downloadFile_missingFile_throwsStorageException() {
        String nonExistent = tempDir.resolve("invoices/missing.txt").toString();

        assertThatThrownBy(() -> service.downloadFile(nonExistent))
                .isInstanceOf(StorageException.class);
    }

    // ── path traversal protection ────────────────────────────────────────────────

    @Test
    void downloadFile_pathOutsideRoot_throwsStorageException() throws IOException {
        Path outsideFile = tempDir.getParent().resolve("evil.txt");
        Files.writeString(outsideFile, "evil");

        assertThatThrownBy(() -> service.downloadFile(outsideFile.toString()))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("outside local storage root");
    }

    @Test
    void deleteFile_pathOutsideRoot_throwsStorageException() throws IOException {
        Path outsideFile = tempDir.getParent().resolve("evil2.txt");
        Files.writeString(outsideFile, "evil");

        assertThatThrownBy(() -> service.deleteFile(outsideFile.toString()))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("outside local storage root");
    }

    // ── deleteFile ───────────────────────────────────────────────────────────────

    @Test
    void deleteFile_removesExistingFile() throws IOException {
        byte[] data = "to be deleted".getBytes(StandardCharsets.UTF_8);
        String path = service.uploadBytes(data, "delete_me.txt", "invoices");

        service.deleteFile(path);

        assertThat(Path.of(path)).doesNotExist();
    }

    @Test
    void deleteFile_nonExistentFile_doesNotThrow() {
        String path = tempDir.resolve("invoices/gone.txt").toString();
        service.deleteFile(path); // must not throw
    }

    // ── uploadFile (MultipartFile) ───────────────────────────────────────────────

    @Test
    void uploadFile_storesContentsAndReturnsPath() {
        byte[] content = "game image data".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mock = new MockMultipartFile("file", "cover.png",
                "image/png", content);

        String path = service.uploadFile(mock, "games/images");

        assertThat(tempDir.resolve(path)).exists();
        assertThat(Path.of(path).getParent()).isEqualTo(Path.of("games/images"));
    }

    @Test
    void uploadFile_emptyFilename_throwsIllegalArgument() {
        MockMultipartFile mock = new MockMultipartFile("file", "",
                "image/png", new byte[0]);

        assertThatThrownBy(() -> service.uploadFile(mock, "games/images"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
