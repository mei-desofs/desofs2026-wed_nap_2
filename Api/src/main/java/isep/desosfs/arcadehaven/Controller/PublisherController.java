package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping("/games/{gameId}/files")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID gameId,
                                               @RequestParam String path) throws Exception {

        byte[] fileData = gameService.downloadGameFile(gameId, path);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + extractFilename(path) + "\"")
                .body(fileData);
    }

    @PostMapping("/games/{id}/files")
    public ResponseEntity<GameResponse> uploadFile(@PathVariable UUID id,
                                                    @RequestParam MultipartFile file,
                                                    @RequestParam(defaultValue = "IMAGE") FileType fileType) throws Exception {
        return ResponseEntity.ok(gameService.uploadGameFile(id, file, fileType));
    }

    private String extractFilename(String path) {
        if (path == null) return "file";
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
