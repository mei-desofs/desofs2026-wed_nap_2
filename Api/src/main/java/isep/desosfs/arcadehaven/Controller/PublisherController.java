package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.FileDownloadResult;
import isep.desosfs.arcadehaven.Dto.Response.GameMetricsResponse;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/publisher")
public class PublisherController {

    private final GameService gameService;

    public PublisherController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/games")
    public ResponseEntity<List<GameResponse>> getMyGames() {
        return ResponseEntity.ok(gameService.getMyGames());
    }

    @PostMapping("/games")
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(request));
    }

    @PutMapping("/games/{id}")
    public ResponseEntity<GameResponse> updateGame(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateGameRequest request) {
        return ResponseEntity.ok(gameService.updateGame(id, request));
    }

    @GetMapping("/games/{gameId}/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID gameId,
                                               @RequestParam UUID file) {
        FileDownloadResult fileData = gameService.downloadGameFile(gameId, file);

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"" + fileData.filename() + "\""
                )
                .body(fileData.data());
    }

    @GetMapping("/games/{id}/metrics")
    public ResponseEntity<GameMetricsResponse> getGameMetrics(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.getGameMetrics(id));
    }

    @PostMapping("/games/{id}/files")
    public ResponseEntity<GameResponse> uploadFile(@PathVariable UUID id,
                                                    @RequestParam MultipartFile file,
                                                    @RequestParam(defaultValue = "IMAGE") FileType fileType) {
        return ResponseEntity.ok(gameService.uploadGameFile(id, file, fileType));
    }

    private String safeFilename(String filename) {
        if (filename == null) return "download";

        return filename
                .replaceAll("[\\r\\n\"\\\\]", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
