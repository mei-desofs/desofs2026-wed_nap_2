package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private GameService gameService;

    private User publisher;
    private Game pendingGame;

    @BeforeEach
    void setUp() {
        publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        pendingGame = Game.create("Test Game", "desc", BigDecimal.TEN, null, publisher);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("pub");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findByUsername("pub")).thenReturn(Optional.of(publisher));
    }

    @Test
    void createGame_returnsGameAsPending() {
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateGameRequest request = new CreateGameRequest("Test Game", "desc",
                BigDecimal.TEN, null);
        GameResponse response = gameService.createGame(request);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.title()).isEqualTo("Test Game");
    }

    @Test
    void approveGame_pendingGame_becomesActive() {
        UUID id = pendingGame.getId();
        when(gameRepository.findById(id)).thenReturn(Optional.of(pendingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameResponse response = gameService.approveGame(id);

        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void approveGame_notFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.approveGame(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Game not found");
    }

    @Test
    void rejectGame_pendingGame_becomesRejected() {
        UUID id = pendingGame.getId();
        when(gameRepository.findById(id)).thenReturn(Optional.of(pendingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameResponse response = gameService.rejectGame(id);

        assertThat(response.status()).isEqualTo("REJECTED");
    }

    @Test
    void removeGame_anyGame_becomesRemoved() {
        pendingGame.approve();
        UUID id = pendingGame.getId();
        when(gameRepository.findById(id)).thenReturn(Optional.of(pendingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameResponse response = gameService.removeGame(id);

        assertThat(response.status()).isEqualTo("REMOVED");
    }

    @Test
    void getGameById_notFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGameById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Game not found");
    }

    @Test
    void getGameById_found_returnsResponse() {
        UUID id = pendingGame.getId();
        when(gameRepository.findById(id)).thenReturn(Optional.of(pendingGame));

        GameResponse response = gameService.getGameById(id);

        assertThat(response.title()).isEqualTo("Test Game");
    }

    @Test
    void updateGame_notOwnedByPublisher_throwsResourceNotFound() {
        UUID id = pendingGame.getId();
        when(gameRepository.findByIdAndPublisher(id, publisher)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.updateGame(id, new UpdateGameRequest("New", null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Game not found or not owned by you");
    }

    @Test
    void updateGame_success_updatesFields() {
        UUID id = pendingGame.getId();
        when(gameRepository.findByIdAndPublisher(id, publisher))
                .thenReturn(Optional.of(pendingGame));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameResponse response = gameService.updateGame(id,
                new UpdateGameRequest("New Title", "New desc", new BigDecimal("19.99")));

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("19.99"));
    }

    @Test
    void getAllActiveGames_withFilters_delegatesToRepository() {
        Game activeGame = Game.create("Shooter", "desc", new BigDecimal("15"), null, publisher);
        activeGame.approve();
        when(gameRepository.findActiveWithFilters("shooter", BigDecimal.ONE, new BigDecimal("20")))
                .thenReturn(List.of(activeGame));

        List<GameResponse> results = gameService.getAllActiveGames("shooter",
                BigDecimal.ONE, new BigDecimal("20"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("Shooter");
    }

    @Test
    void getAllActiveGames_noFilters_returnsAll() {
        when(gameRepository.findActiveWithFilters(null, null, null))
                .thenReturn(List.of(pendingGame));

        List<GameResponse> results = gameService.getAllActiveGames(null, null, null);

        assertThat(results).hasSize(1);
    }
}
