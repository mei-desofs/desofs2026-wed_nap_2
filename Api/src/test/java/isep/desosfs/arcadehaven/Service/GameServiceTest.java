package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.OrderRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock FileStorageService fileStorageService;
    @Mock OrderRepository orderRepository;

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

        var result = gameService.getAllActiveGames(null, null, null, null);

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

        var result = gameService.getAllActiveGames(null, null, null, null);

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

        Game game = Game.create("t", "d", BigDecimal.TEN, "r", null, user);

        byte[] expected = new byte[]{1, 2, 3};

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.of(game));

        when(fileStorageService.downloadFile(anyString()))
                .thenReturn(expected);

        byte[] result =
                gameService.downloadGameFile(UUID.randomUUID(), "path");

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowWhenDownloadingGameNotOwned() {

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.downloadGameFile(UUID.randomUUID(), "path"));
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

        MockMultipartFile broken =
                new MockMultipartFile(
                        "f",
                        "img.png",
                        "image/png",
                        new byte[0]
                );

        lenient().when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                gameService.uploadGameFile(UUID.randomUUID(), broken, FileType.IMAGE));
    }

    @Test
    void shouldThrowWhenInvalidMimeType() {

        byte[] txt = "hello".getBytes();

        MockMultipartFile file =
                new MockMultipartFile("f", "file.txt", "text/plain", txt);

        lenient().when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        lenient().when(gameRepository.findByIdAndPublisher(any(), eq(user)))
                .thenReturn(Optional.of(
                        Game.create("t", "d", BigDecimal.TEN, "r", null, user)
                ));

        assertThrows(IllegalArgumentException.class, () ->
                gameService.uploadGameFile(UUID.randomUUID(), file, FileType.IMAGE));
    }
}
