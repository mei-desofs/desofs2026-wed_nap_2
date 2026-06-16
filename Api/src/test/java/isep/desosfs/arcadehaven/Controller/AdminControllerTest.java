package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Service.AdminService;
import isep.desosfs.arcadehaven.Service.GameService;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {
    @Mock
    private AdminService adminService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private AdminController controller;

    @Test
    void shouldGetAllUsers() {
        List<UserResponse> users = List.of(createUserResponse());

        when(adminService.getAllUsers()).thenReturn(users);

        var response = controller.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(users, response.getBody());

        verify(adminService).getAllUsers();
    }

    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();

        UserResponse user = createUserResponse();

        when(adminService.getUserById(id)).thenReturn(user);

        var response = controller.getUser(id);

        assertEquals(user, response.getBody());

        verify(adminService).getUserById(id);
    }

    @Test
    void shouldActivateUser() {
        UUID id = UUID.randomUUID();

        UserResponse user = createUserResponse();

        when(adminService.activateUser(id)).thenReturn(user);

        var response = controller.activateUser(id);

        assertEquals(user, response.getBody());

        verify(adminService).activateUser(id);
    }

    @Test
    void shouldDeactivateUser() {
        UUID id = UUID.randomUUID();

        UserResponse user = new UserResponse(
                UUID.randomUUID(),
                "user",
                "user@mail.com",
                "USER",
                false,
                LocalDateTime.now()
        );

        when(adminService.deactivateUser(id)).thenReturn(user);

        var response = controller.deactivateUser(id);

        assertEquals(user, response.getBody());

        verify(adminService).deactivateUser(id);
    }

    @Test
    void shouldChangeUserRole() {
        UUID id = UUID.randomUUID();

        UserResponse user = new UserResponse(
                UUID.randomUUID(),
                "admin",
                "admin@mail.com",
                "ADMIN",
                true,
                LocalDateTime.now()
        );

        when(adminService.changeUserRole(id, Role.ADMIN))
                .thenReturn(user);

        var response = controller.changeUserRole(id, Role.ADMIN);

        assertEquals(user, response.getBody());

        verify(adminService).changeUserRole(id, Role.ADMIN);
    }

    @Test
    void shouldGetAllGames() {
        List<GameResponse> games = List.of(createGameResponse());

        when(gameService.getAllGames()).thenReturn(games);

        var response = controller.getAllGames();

        assertEquals(games, response.getBody());

        verify(gameService).getAllGames();
    }

    @Test
    void shouldApproveGame() {
        UUID id = UUID.randomUUID();

        GameResponse game = createGameResponse();

        when(gameService.approveGame(id)).thenReturn(game);

        var response = controller.approveGame(id);

        assertEquals(game, response.getBody());

        verify(gameService).approveGame(id);
    }

    @Test
    void shouldRemoveGame() {
        UUID id = UUID.randomUUID();

        GameResponse game = createGameResponse();

        when(gameService.removeGame(id)).thenReturn(game);

        var response = controller.removeGame(id);

        assertEquals(game, response.getBody());

        verify(gameService).removeGame(id);
    }

    private UserResponse createUserResponse() {
        return new UserResponse(
                UUID.randomUUID(),
                "user",
                "user@mail.com",
                "USER",
                true,
                LocalDateTime.now()
        );
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
                null,
                "publisher",
                LocalDateTime.now()
        );
    }
}
