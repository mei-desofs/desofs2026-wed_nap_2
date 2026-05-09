package isep.desosfs.arcadehaven.Dto.Response;

import isep.desosfs.arcadehaven.Domain.Library;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LibraryResponse(
        UUID id,
        List<LibraryEntryResponse> entries,
        LocalDateTime createdAt
) {
    public static LibraryResponse from(Library library) {
        return new LibraryResponse(
                library.getId(),
                library.getEntries().stream().map(LibraryEntryResponse::from).toList(),
                library.getCreatedAt()
        );
    }
}
