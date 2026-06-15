package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Edge-case tests for GameFile embeddable.
 * Complements GameFileTest with all FileType variants and field boundary checks.
 */
class GameFileTest {

    // ── Field storage ────────────────────────────────────────────────────────

    @Test
    void create_storesFilenameExactly() {
        GameFile file = new GameFile(UUID.randomUUID(), "cover.jpg", "/covers/cover.jpg", FileType.COVER, LocalDateTime.now());
        assertThat(file.getFilename()).isEqualTo("cover.jpg");
    }

    @Test
    void create_storesPathExactly() {
        GameFile file = new GameFile(UUID.randomUUID(), "s.png", "/screenshots/s.png", FileType.SCREENSHOT, LocalDateTime.now());
        assertThat(file.getPath()).isEqualTo("/screenshots/s.png");
    }

    @Test
    void create_storesUploadedAtExactly() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30);
        GameFile file = new GameFile(UUID.randomUUID(), "img.png", "/img.png", FileType.IMAGE, timestamp);
        assertThat(file.getUploadedAt()).isEqualTo(timestamp);
    }

    // ── All FileType variants ────────────────────────────────────────────────

    @ParameterizedTest
    @EnumSource(FileType.class)
    void create_allFileTypes_storedCorrectly(FileType type) {
        GameFile file = new GameFile(UUID.randomUUID(), "file", "/path", type, LocalDateTime.now());
        assertThat(file.getFileType()).isEqualTo(type);
    }

    @Test
    void create_withImageType_hasCorrectType() {
        GameFile file = new GameFile(UUID.randomUUID(), "img.png", "/img.png", FileType.IMAGE, LocalDateTime.now());
        assertThat(file.getFileType()).isEqualTo(FileType.IMAGE);
    }

    @Test
    void create_withScreenshotType_hasCorrectType() {
        GameFile file = new GameFile(UUID.randomUUID(), "shot.png", "/shot.png", FileType.SCREENSHOT, LocalDateTime.now());
        assertThat(file.getFileType()).isEqualTo(FileType.SCREENSHOT);
    }

    @Test
    void create_withCoverType_hasCorrectType() {
        GameFile file = new GameFile(UUID.randomUUID(), "cover.jpg", "/cover.jpg", FileType.COVER, LocalDateTime.now());
        assertThat(file.getFileType()).isEqualTo(FileType.COVER);
    }

    // ── Timestamp freshness ──────────────────────────────────────────────────

    @Test
    void create_uploadedAtIsClose_toNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        GameFile file = new GameFile(UUID.randomUUID(), "img.png", "/img.png", FileType.IMAGE, LocalDateTime.now());
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(file.getUploadedAt()).isAfterOrEqualTo(before);
        assertThat(file.getUploadedAt()).isBeforeOrEqualTo(after);
    }

    // ── Path formats ─────────────────────────────────────────────────────────

    @Test
    void create_absolutePath_storedVerbatim() {
        String path = "/games/images/uuid/img.png";
        GameFile file = new GameFile(UUID.randomUUID(), "img.png", path, FileType.IMAGE, LocalDateTime.now());
        assertThat(file.getPath()).isEqualTo(path);
    }

    @Test
    void create_relativePathWithSubdirectories_storedVerbatim() {
        String path = "games/covers/uuid/cover.jpg";
        GameFile file = new GameFile(UUID.randomUUID(), "cover.jpg", path, FileType.COVER, LocalDateTime.now());
        assertThat(file.getPath()).isEqualTo(path);
    }

    // ── Distinctness ─────────────────────────────────────────────────────────

    @Test
    void twoFiles_withDifferentTypes_areDistinct() {
        LocalDateTime now = LocalDateTime.now();
        GameFile image = new GameFile(UUID.randomUUID(), "img.png", "/img.png", FileType.IMAGE, now);
        GameFile cover = new GameFile(UUID.randomUUID(), "img.png", "/img.png", FileType.COVER, now);

        assertThat(image.getFileType()).isNotEqualTo(cover.getFileType());
    }

    @Test
    void shouldCreateGameFile() {
        LocalDateTime now = LocalDateTime.now();

        GameFile file = new GameFile(
                UUID.randomUUID(),
                "image.png",
                "/images/image.png",
                FileType.IMAGE,
                now
        );

        assertEquals("image.png", file.getFilename());
        assertEquals("/images/image.png", file.getPath());
        assertEquals(FileType.IMAGE, file.getFileType());
        assertEquals(now, file.getUploadedAt());
    }
}
