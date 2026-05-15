package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Service.GameService;

@ExtendWith(MockitoExtension.class)
public class PublisherControllerTest {
    @Mock
    private GameService gameService;

    @InjectMocks
    private PublisherController controller;

    @Test
    void shouldGetMyGames() {
        List<GameResponse> games = List.of(createGameResponse());

        when(gameService.getMyGames()).thenReturn(games);

        var response = controller.getMyGames();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(games, response.getBody());

        verify(gameService).getMyGames();
    }

    @Test
    void shouldCreateGame() {
        CreateGameRequest request = new CreateGameRequest(
                "Game",
                "Description",
                BigDecimal.TEN,
                "rawg-id"
        );

        GameResponse game = createGameResponse();

        when(gameService.createGame(request)).thenReturn(game);

        var response = controller.createGame(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(game, response.getBody());

        verify(gameService).createGame(request);
    }

    @Test
    void shouldUpdateGame() {
        UUID id = UUID.randomUUID();

        UpdateGameRequest request = new UpdateGameRequest(
                "Updated Game",
                "Updated Description",
                BigDecimal.valueOf(20)
        );

        GameResponse game = createGameResponse();

        when(gameService.updateGame(id, request)).thenReturn(game);

        var response = controller.updateGame(id, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(game, response.getBody());

        verify(gameService).updateGame(id, request);
    }

    @Test
    void shouldUploadImageFile() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "content".getBytes()
        );

        GameResponse game = createGameResponse();

        when(gameService.uploadGameFile(id, file, FileType.IMAGE))
                .thenReturn(game);

        var response = controller.uploadFile(id, file, FileType.IMAGE);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(game, response.getBody());

        verify(gameService).uploadGameFile(id, file, FileType.IMAGE);
    }

    @Test
    void shouldUploadScreenshotFile() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "screenshot.png",
                "image/png",
                "content".getBytes()
        );

        GameResponse game = createGameResponse();

        when(gameService.uploadGameFile(id, file, FileType.SCREENSHOT))
                .thenReturn(game);

        var response = controller.uploadFile(id, file, FileType.SCREENSHOT);

        assertEquals(game, response.getBody());

        verify(gameService).uploadGameFile(id, file, FileType.SCREENSHOT);
    }

    @Test
    void shouldUploadCoverFile() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                "content".getBytes()
        );

        GameResponse game = createGameResponse();

        when(gameService.uploadGameFile(id, file, FileType.COVER))
                .thenReturn(game);

        var response = controller.uploadFile(id, file, FileType.COVER);

        assertEquals(game, response.getBody());

        verify(gameService).uploadGameFile(id, file, FileType.COVER);
    }

    @Test
    void shouldThrowExceptionWhenMimeTypeIsNull() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",
                null,
                "content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.IMAGE)
        );

        assertEquals("Missing MIME type", exception.getMessage());

        verify(gameService, never())
                .uploadGameFile(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionForInvalidImageMimeType() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.pdf",
                "application/pdf",
                "content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.IMAGE)
        );

        assertEquals(
                "Invalid image MIME type: application/pdf",
                exception.getMessage()
        );

        verify(gameService, never())
                .uploadGameFile(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionForInvalidScreenshotMimeType() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.pdf",
                "application/pdf",
                "content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.SCREENSHOT)
        );

        assertEquals(
                "Invalid screenshot MIME type: application/pdf",
                exception.getMessage()
        );

        verify(gameService, never())
                .uploadGameFile(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionForInvalidCoverMimeType() throws IOException {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.pdf",
                "application/pdf",
                "content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.COVER)
        );

        assertEquals(
                "Invalid cover MIME type: application/pdf",
                exception.getMessage()
        );

        verify(gameService, never())
                .uploadGameFile(any(), any(), any());
    }

    private GameResponse createGameResponse() {
        return new GameResponse(
                UUID.randomUUID(),
                "Game",
                "Description",
                BigDecimal.TEN,
                "ACTIVE",
                "rawg-id",
                "publisher",
                LocalDateTime.now()
        );
    }
}
