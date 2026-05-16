package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock FileStorageService fileStorageService;

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

        CreateGameRequest req =
                new CreateGameRequest("t", "d", BigDecimal.TEN, "rawg");

        var result = gameService.createGame(req);

        assertEquals("t", result.title());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        CreateGameRequest req =
                new CreateGameRequest("t", "d", BigDecimal.TEN, "rawg");

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.createGame(req));
    }

    @Test
    void shouldUpdateGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", user);

        UUID id = UUID.randomUUID();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(id, user)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        UpdateGameRequest req = new UpdateGameRequest("new", "desc", BigDecimal.valueOf(20));

        var result = gameService.updateGame(id, req);

        assertEquals("new", result.title());
    }

    @Test
    void shouldThrowWhenUpdateGameNotOwner() {
        UUID id = UUID.randomUUID();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(id, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gameService.updateGame(id, new UpdateGameRequest("a","b",BigDecimal.TEN)));
    }

    @Test
    void shouldApproveGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", user);

        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        gameService.approveGame(UUID.randomUUID());

        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldRemoveGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", user);

        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);

        gameService.removeGame(UUID.randomUUID());

        assertEquals(GameStatus.REMOVED, game.getStatus());
    }

    @Test
    void shouldUploadFile() throws Exception {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", user);

        MockMultipartFile file =
                new MockMultipartFile("f", "img.png", "image/png", "x".getBytes());

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(gameRepository.findByIdAndPublisher(any(), eq(user))).thenReturn(Optional.of(game));
        when(fileStorageService.saveFile(any(), any())).thenReturn("path");
        when(gameRepository.save(any())).thenReturn(game);

        var result = gameService.uploadGameFile(UUID.randomUUID(), file, FileType.IMAGE);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenApproveNonPendingGame() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", user);
        game.approve();

        assertThrows(IllegalStateException.class, game::approve);
    }

    @Test
    void shouldRejectInvalidPrice() {
        Game game = Game.create("t", "d", BigDecimal.TEN, "r", user);

        assertThrows(IllegalArgumentException.class,
                () -> game.updatePrice(BigDecimal.ZERO));
    }

    @Test
    void shouldChangeRoleSuccessfully() {
        User user = User.create("u","e","p", Role.BUYER);

        user.changeRole(Role.PUBLISHER);

        assertEquals(Role.PUBLISHER, user.getRole());
    }

    @Test
    void shouldThrowWhenChangingAdminRole() {
        User admin = User.create("a","e","p", Role.ADMIN);

        assertThrows(IllegalStateException.class,
                () -> admin.changeRole(Role.BUYER));
    }

    @Test
    void shouldGetAllActiveGames() {
        when(gameRepository.findByStatus(GameStatus.ACTIVE))
                .thenReturn(List.of());

        var result = gameService.getAllActiveGames();

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
        when(gameRepository.findByStatus(GameStatus.ACTIVE))
                .thenReturn(List.of());

        var result = gameService.getAllActiveGames();

        assertTrue(result.isEmpty());
    }
}
