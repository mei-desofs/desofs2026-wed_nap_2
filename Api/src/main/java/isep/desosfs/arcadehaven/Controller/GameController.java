package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getAllGames(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(gameService.getAllActiveGames(title, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }
}
