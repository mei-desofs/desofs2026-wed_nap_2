package isep.desosfs.arcadehaven.Domain;


import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
public class GameFile {

    private UUID id;
    private String filename;
    private String path;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    protected GameFile() {}
}
