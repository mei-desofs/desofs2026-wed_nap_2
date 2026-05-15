package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;

public class GameFileTest {
    @Test
    void shouldCreateGameFile() {
        LocalDateTime now = LocalDateTime.now();

        GameFile file = new GameFile(
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
