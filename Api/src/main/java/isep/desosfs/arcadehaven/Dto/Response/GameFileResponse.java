package isep.desosfs.arcadehaven.Dto.Response;

import isep.desosfs.arcadehaven.Domain.GameFile;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameFileResponse(UUID id, String filename, String fileType, LocalDateTime uploadedAt) {
    public static GameFileResponse from(GameFile f) {
        return new GameFileResponse(f.getId(), f.getFilename(), f.getFileType().name(), f.getUploadedAt());
    }
}
