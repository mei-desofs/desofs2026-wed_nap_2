package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import isep.desosfs.arcadehaven.Dto.Response.FileDownloadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.GameMetricsResponse;
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
                "rawg-id",
                null
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
                BigDecimal.valueOf(20),
                null
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
    void shouldThrowExceptionWhenMimeTypeIsNull() {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.png",
                null,
                "content".getBytes()
        );

        when(gameService.uploadGameFile(eq(id), any(), eq(FileType.IMAGE)))
                .thenThrow(new IllegalArgumentException("Cannot detect file type"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.IMAGE));
    }

    @Test
    void shouldThrowExceptionForInvalidImageMimeType() {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.pdf",
                "application/pdf",
                "content".getBytes()
        );

        when(gameService.uploadGameFile(eq(id), any(), eq(FileType.IMAGE)))
                .thenThrow(new IllegalArgumentException("Invalid file type for IMAGE: application/pdf"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.IMAGE));
    }

    @Test
    void shouldThrowExceptionForInvalidScreenshotMimeType() {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.pdf",
                "application/pdf",
                "content".getBytes()
        );

        when(gameService.uploadGameFile(eq(id), any(), eq(FileType.SCREENSHOT)))
                .thenThrow(new IllegalArgumentException("Invalid file type for SCREENSHOT: application/pdf"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.SCREENSHOT));
    }

    @Test
    void shouldThrowExceptionForInvalidCoverMimeType() {
        UUID id = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.pdf",
                "application/pdf",
                "content".getBytes()
        );

        when(gameService.uploadGameFile(eq(id), any(), eq(FileType.COVER)))
                .thenThrow(new IllegalArgumentException("Invalid file type for COVER: application/pdf"));

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadFile(id, file, FileType.COVER));
    }

    @Test
    void shouldDownloadFile_returnsFileDataAndFilename() {

        UUID gameId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        byte[] fileData = "data".getBytes();

        FileDownloadResult result = new FileDownloadResult("file.png", fileData);

        when(gameService.downloadGameFile(gameId, fileId))
                .thenReturn(result);

        var response = controller.downloadFile(gameId, fileId);

        assertEquals(fileData, response.getBody());
        assertTrue(response.getHeaders()
                .getFirst("Content-Disposition")
                .contains("file.png"));

        verify(gameService).downloadGameFile(gameId, fileId);
    }

    @Test
    void shouldDownloadFile_usesStoredFilename() {
        UUID gameId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        byte[] fileData = "data".getBytes();

        FileDownloadResult result = new FileDownloadResult("filename.txt", fileData);

        when(gameService.downloadGameFile(gameId, fileId))
                .thenReturn(result);

        var response = controller.downloadFile(gameId, fileId);

        assertEquals(fileData, response.getBody());
        assertTrue(response.getHeaders()
                .getFirst("Content-Disposition")
                .contains("filename.txt"));

        verify(gameService).downloadGameFile(gameId, fileId);
    }

    @Test
    void shouldGetGameMetrics_returnsMetrics() {
        UUID gameId = UUID.randomUUID();
        GameMetricsResponse metrics = new GameMetricsResponse(
                gameId,
                "Test Game",  
                10L,           
                BigDecimal.TEN 
        );

        when(gameService.getGameMetrics(gameId)).thenReturn(metrics);

        var response = controller.getGameMetrics(gameId);

        assertEquals(metrics, response.getBody());
        verify(gameService).getGameMetrics(gameId);
    }

    private GameResponse createGameResponse() {
        return new GameResponse(
                UUID.randomUUID(),
                "Game",
                "Description",
                BigDecimal.TEN,
                "ACTIVE",
                "rawg-id",
                null,
                "publisher",
                LocalDateTime.now()
        );
    }
}
