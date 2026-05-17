package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Dto.Request.ImportKeyRequest;
import isep.desosfs.arcadehaven.Dto.Response.LibraryResponse;
import isep.desosfs.arcadehaven.Service.LibraryService;

@ExtendWith(MockitoExtension.class)
public class LibraryControllerTest {
    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private LibraryController controller;

    @Test
    void shouldGetMyLibrary() {
        LibraryResponse library = new LibraryResponse(
                UUID.randomUUID(),
                List.of(),
                LocalDateTime.now()
        );

        when(libraryService.getMyLibrary()).thenReturn(library);

        var response = controller.getMyLibrary();

        assertEquals(library, response.getBody());

        verify(libraryService).getMyLibrary();
    }

    @Test
    void shouldImportGameKey() {
        UUID gameId = UUID.randomUUID();
        ImportKeyRequest request = new ImportKeyRequest(gameId, "KEY-XYZ-123");

        LibraryResponse library = new LibraryResponse(
                UUID.randomUUID(),
                List.of(),
                LocalDateTime.now()
        );

        when(libraryService.importGameKey(request)).thenReturn(library);

        var response = controller.importGameKey(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(library, response.getBody());
        verify(libraryService).importGameKey(request);
    }
}
