package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.OrderRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RNF-23 — validates that game listing delegates to the indexed query findActiveWithFilters,
 * which is backed by composite DB indexes (V4 migration) for < 500 ms response times.
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private GameService gameService;

    private Game activeGame() {
        User publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        Game g = Game.create("Sonic", "desc", BigDecimal.TEN, null, "action", publisher);
        g.approve();
        return g;
    }

    @Test
    void getAllActiveGames_noFilters_delegatesToFindActiveWithFilters() {
        when(gameRepository.findActiveWithFilters(null, null, null, null))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(null, null, null, null);

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters(null, null, null, null);
    }

    @Test
    void getAllActiveGames_withTitleFilter_passesToRepository() {
        when(gameRepository.findActiveWithFilters("Sonic", null, null, null))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames("Sonic", null, null, null);

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters("Sonic", null, null, null);
    }

    @Test
    void getAllActiveGames_withCategoryFilter_passesToRepository() {
        when(gameRepository.findActiveWithFilters(null, "action", null, null))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(null, "action", null, null);

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters(null, "action", null, null);
    }

    @Test
    void getAllActiveGames_withPriceRange_passesToRepository() {
        BigDecimal min = BigDecimal.ONE;
        BigDecimal max = BigDecimal.valueOf(50);
        when(gameRepository.findActiveWithFilters(null, null, min, max))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(null, null, min, max);

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters(null, null, min, max);
    }

    @Test
    void getAllActiveGames_withAllFilters_passesToRepository() {
        BigDecimal min = BigDecimal.ONE;
        BigDecimal max = BigDecimal.valueOf(20);
        when(gameRepository.findActiveWithFilters("Sonic", "action", min, max))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames("Sonic", "action", min, max);

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters("Sonic", "action", min, max);
    }

    @Test
    void getAllActiveGames_emptyResult_returnsEmptyList() {
        when(gameRepository.findActiveWithFilters(null, null, null, null))
                .thenReturn(List.of());

        List<GameResponse> result = gameService.getAllActiveGames(null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllActiveGames_mapsToGameResponse() {
        Game game = activeGame();
        when(gameRepository.findActiveWithFilters(null, null, null, null))
                .thenReturn(List.of(game));

        List<GameResponse> result = gameService.getAllActiveGames(null, null, null, null);

        assertThat(result.get(0).title()).isEqualTo("Sonic");
        assertThat(result.get(0).price()).isEqualByComparingTo(BigDecimal.TEN);
    }
}
