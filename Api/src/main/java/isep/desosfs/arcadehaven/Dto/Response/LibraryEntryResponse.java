package isep.desosfs.arcadehaven.Dto.Response;

import isep.desosfs.arcadehaven.Domain.LibraryEntry;

import java.time.LocalDateTime;
import java.util.UUID;

public record LibraryEntryResponse(
        UUID id,
        UUID gameId,
        String gameTitle,
        String activationKey,
        String status,
        LocalDateTime acquiredAt
) {
    public static LibraryEntryResponse from(LibraryEntry entry) {
        return new LibraryEntryResponse(
                entry.getId(),
                entry.getGame().getId(),
                entry.getGame().getTitle(),
                entry.getActivationKey(),
                entry.getStatus().name(),
                entry.getAcquiredAt()
        );
    }
}
