package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import isep.desosfs.arcadehaven.Dto.Request.GameFilterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Service.GameService;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {
    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController controller;

    @Test
    void shouldGetAllGames() {
        List<GameResponse> games = List.of(createGameResponse());

        GameFilterRequest request = new GameFilterRequest(null, null, null, null);

        when(gameService.getAllActiveGames(request)).thenReturn(games);

        var response = controller.getAllGames(request);

        assertEquals(games, response.getBody());

        verify(gameService).getAllActiveGames(request);
    }

    @Test
    void shouldGetGameById() {
        UUID id = UUID.randomUUID();

        GameResponse game = createGameResponse();

        when(gameService.getGameById(id)).thenReturn(game);

        var response = controller.getGame(id);

        assertEquals(game, response.getBody());

        verify(gameService).getGameById(id);
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
