package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import isep.desosfs.arcadehaven.Domain.GameFile;
import isep.desosfs.arcadehaven.Dto.Request.GameFilterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Exception.StorageException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.OrderRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.ClamAVService;
import isep.desosfs.arcadehaven.Validation.FileValidator;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RNF-23 — validates that game listing delegates to the indexed query findActiveWithFilters,
 * which is backed by composite DB indexes (V4 migration) for < 500 ms response times.
 */
@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock FileStorageService fileStorageService;
    @Mock OrderRepository orderRepository;
    @Mock FileValidator fileValidator;
    @Mock ClamAVService clamAVService;

    @InjectMocks GameService gameService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.create("john", "mail", "pass", Role.PUBLISHER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john", null)
        );
    }

    @Test
    void shouldCreateGame() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreateGameRequest req = new CreateGameRequest("t", "d", BigDecimal.TEN, "rawg", null);

        var result = gameService.createGame(req);

        assertEquals("t", result.title());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        CreateGameRequest req = new CreateGameRequest("t", "d", BigDecimal.TEN, "rawg", null);

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.createGame(req));
    }

    @Test
    void shouldUpdateGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        UUID id = UUID.randomUUID();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(id, user)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        UpdateGameRequest req = new UpdateGameRequest("new", "desc", BigDecimal.valueOf(20), null);

        var result = gameService.updateGame(id, req);

        assertEquals("new", result.title());
    }

    @Test
    void shouldThrowWhenUpdateGameNotOwner() {
        UUID id = UUID.randomUUID();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(id, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.updateGame(id, new UpdateGameRequest("a", "b", BigDecimal.TEN, null)));
    }

    @Test
    void shouldApproveGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        gameService.approveGame(UUID.randomUUID());

        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldRemoveGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        gameService.removeGame(UUID.randomUUID());

        assertEquals(GameStatus.REMOVED, game.getStatus());
    }

    @Test
    void shouldUploadFile() throws Exception {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        // PNG magic bytes so Tika detects image/png, not text/plain
        byte[] pngBytes = {(byte)0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A,0,0,0,0x0D,0x49,0x48,0x44,0x52};
        MockMultipartFile file =
                new MockMultipartFile("f", "img.png", "image/png", pngBytes);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(any(), eq(user))).thenReturn(Optional.of(game));
        when(fileStorageService.saveFile(any(), any())).thenReturn("path");
        when(gameRepository.save(any())).thenReturn(game);

        var result = gameService.uploadGameFile(UUID.randomUUID(), file, FileType.IMAGE);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenApproveNonPendingGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);
        game.approve();

        assertThrows(IllegalStateException.class, game::approve);
    }

    @Test
    void shouldRejectInvalidPrice() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        assertThrows(IllegalArgumentException.class,
                () -> game.updatePrice(BigDecimal.ZERO));
    }

    @Test
    void shouldChangeRoleSuccessfully() {
        User u = User.create("u", "e", "p", Role.BUYER);

        u.changeRole(Role.PUBLISHER);

        assertEquals(Role.PUBLISHER, u.getRole());
    }

    @Test
    void shouldThrowWhenChangingAdminRole() {
        User admin = User.create("a", "e", "p", Role.ADMIN);

        assertThrows(IllegalStateException.class,
                () -> admin.changeRole(Role.BUYER));
    }

    @Test
    void shouldGetAllActiveGames() {
        when(gameRepository.findActiveWithFilters(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        GameFilterRequest req = new GameFilterRequest(null, null, null, null);
        var result = gameService.getAllActiveGames(req);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenGameNotFound() {
        when(gameRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.getGameById(UUID.randomUUID()));
    }

    @Test
    void shouldReturnAllGames() {
        when(gameRepository.findAll()).thenReturn(List.of());

        var result = gameService.getAllGames();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnActiveGames() {
        when(gameRepository.findActiveWithFilters(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        GameFilterRequest req = new GameFilterRequest(null, null, null, null);

        var result = gameService.getAllActiveGames(req);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectGame() {

        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        gameService.rejectGame(UUID.randomUUID());

        assertEquals(GameStatus.REJECTED, game.getStatus());
    }

    @Test
    void shouldDownloadGameFile() {

        UUID gameId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        GameFile file = new GameFile(
                fileId,
                "file.png",
                "games/images/file.png",
                FileType.IMAGE,
                LocalDateTime.now()
        );

        game.addFile(file);

        byte[] expected = new byte[]{1, 2, 3};

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.of(game));

        when(fileStorageService.downloadFile(file.getPath()))
                .thenReturn(expected);

        var result = gameService.downloadGameFile(gameId, fileId);

        assertEquals(expected, result.data());
    }

    @Test
    void shouldThrowWhenDownloadingGameNotOwned() {

        UUID gameId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.downloadGameFile(gameId, fileId));
    }

    @Test
    void shouldReturnMyGames() {

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByPublisher(user)).thenReturn(List.of());

        var result = gameService.getMyGames();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnGameMetrics() {

        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        Object[] row = new Object[]{10L, BigDecimal.valueOf(100)};

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.of(game));
        when(orderRepository.findMetricsByGameId(any()))
                .thenReturn(row);

        var result = gameService.getGameMetrics(UUID.randomUUID());

        assertEquals(10L, result.unitsSold());
        assertEquals(BigDecimal.valueOf(100), result.totalRevenue());
    }

    @Test
    void shouldReturnGameMetricsWithNullValues() {

        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        Object[] row = new Object[]{null, null};

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.of(game));
        when(orderRepository.findMetricsByGameId(any()))
                .thenReturn(row);

        var result = gameService.getGameMetrics(UUID.randomUUID());

        assertEquals(0L, result.unitsSold());
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
    }

    @Test
    void shouldThrowStorageExceptionWhenFileReadFails() {
        MockMultipartFile broken = new MockMultipartFile("f", "img.png", "image/png", new byte[0]);

        doThrow(new IllegalArgumentException("Invalid file type: application/octet-stream"))
                .when(fileValidator).validateMimeType(any(), any());

        assertThrows(IllegalArgumentException.class, () ->
                gameService.uploadGameFile(UUID.randomUUID(), broken, FileType.IMAGE));
    }

    @Test
    void shouldThrowWhenInvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile("f", "file.txt", "text/plain", "hello".getBytes());

        doThrow(new IllegalArgumentException("Invalid file type: text/plain"))
                .when(fileValidator).validateMimeType(any(), eq(FileType.IMAGE));

        assertThrows(IllegalArgumentException.class, () ->
                gameService.uploadGameFile(UUID.randomUUID(), file, FileType.IMAGE));
    }

    @Test
    void shouldThrowWhenGetGameByIdNotFound() {
        when(gameRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.getGameById(UUID.randomUUID()));
    }

    @Test
    void shouldUpdateGameWithoutChangingPrice() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);
        UUID id = UUID.randomUUID();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(id, user)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        UpdateGameRequest req = new UpdateGameRequest("novo titulo", "nova desc", null, null);

        var result = gameService.updateGame(id, req);

        assertEquals("novo titulo", result.title());
        assertEquals(BigDecimal.TEN, game.getPrice());
    }

    @Test
    void shouldThrowWhenFileSizeExceedsLimit() {
        MockMultipartFile file = new MockMultipartFile("f", "big.png", "image/png", new byte[1]);

        doThrow(new MaxUploadSizeExceededException(25 * 1024 * 1024L))
                .when(fileValidator).validateFileSize(any());

        assertThrows(MaxUploadSizeExceededException.class,
                () -> gameService.uploadGameFile(UUID.randomUUID(), file, FileType.IMAGE));
    }

    @Test
    void shouldThrowStorageExceptionWhenStreamFails() {
        MultipartFile brokenFile = mock(MultipartFile.class);

        doThrow(new StorageException("Cannot read file", new IOException("disco cheio")))
                .when(fileValidator).validateMimeType(any(), any());

        assertThrows(StorageException.class,
                () -> gameService.uploadGameFile(UUID.randomUUID(), brokenFile, FileType.IMAGE));
    }

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

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest(null, null, null, null));

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters(null, null, null, null);
    }

    @Test
    void getAllActiveGames_withTitleFilter_passesToRepository() {
        when(gameRepository.findActiveWithFilters("Sonic", null, null, null))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest("Sonic", null, null, null));

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters("Sonic", null, null, null);
    }

    @Test
    void getAllActiveGames_withCategoryFilter_passesToRepository() {
        when(gameRepository.findActiveWithFilters(null, "action", null, null))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest(null, "action", null, null));

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters(null, "action", null, null);
    }

    @Test
    void getAllActiveGames_withPriceRange_passesToRepository() {
        BigDecimal min = BigDecimal.ONE;
        BigDecimal max = BigDecimal.valueOf(50);
        when(gameRepository.findActiveWithFilters(null, null, min, max))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest(null, null, min, max));

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters(null, null, min, max);
    }

    @Test
    void getAllActiveGames_withAllFilters_passesToRepository() {
        BigDecimal min = BigDecimal.ONE;
        BigDecimal max = BigDecimal.valueOf(20);
        when(gameRepository.findActiveWithFilters("Sonic", "action", min, max))
                .thenReturn(List.of(activeGame()));

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest("Sonic", "action", min, max));

        assertThat(result).hasSize(1);
        verify(gameRepository).findActiveWithFilters("Sonic", "action", min, max);
    }

    @Test
    void getAllActiveGames_emptyResult_returnsEmptyList() {
        when(gameRepository.findActiveWithFilters(null, null, null, null))
                .thenReturn(List.of());

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest(null, null, null, null));

        assertThat(result).isEmpty();
    }

    @Test
    void getAllActiveGames_mapsToGameResponse() {
        Game game = activeGame();
        when(gameRepository.findActiveWithFilters(null, null, null, null))
                .thenReturn(List.of(game));

        List<GameResponse> result = gameService.getAllActiveGames(new GameFilterRequest(null, null, null, null));

        assertThat(result.get(0).title()).isEqualTo("Sonic");
        assertThat(result.get(0).price()).isEqualByComparingTo(BigDecimal.TEN);
    }
}
