package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.ImportKeyRequest;
import isep.desosfs.arcadehaven.Dto.Response.LibraryResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {
    @Mock private LibraryRepository libraryRepository;
    @Mock private UserRepository userRepository;
    @Mock private GameRepository gameRepository;

    @InjectMocks
    private LibraryService libraryService;

    @Test
    void shouldReturnLibrary() {
        User user = User.create("buyer", "mail", "pass", Role.BUYER);
        Library library = Library.create(user);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));

        var auth = new UsernamePasswordAuthenticationToken(
            "buyer",
            null,
            List.of()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        LibraryResponse response = libraryService.getMyLibrary();

        assertNotNull(response);
    }

    @Test
    void shouldImportGameKey() {
        User user = User.create("buyer", "mail", "pass", Role.BUYER);
        Library library = Library.create(user);
        UUID gameId = UUID.randomUUID();

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, user);
        game.approve();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(libraryRepository.save(any())).thenReturn(library);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of()));

        LibraryResponse response = libraryService.importGameKey(new ImportKeyRequest(gameId, "KEY-123"));

        assertNotNull(response);
    }

    @Test
    void shouldThrowWhenImportingGameNotActive() {
        User user = User.create("buyer", "mail", "pass", Role.BUYER);
        Library library = Library.create(user);
        UUID gameId = UUID.randomUUID();

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, user);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of()));

        assertThrows(BusinessException.class,
                () -> libraryService.importGameKey(new ImportKeyRequest(gameId, "KEY-123")));
    }

    @Test
    void shouldThrowWhenGameNotFoundOnImport() {
        User user = User.create("buyer", "mail", "pass", Role.BUYER);
        Library library = Library.create(user);
        UUID gameId = UUID.randomUUID();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of()));

        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.importGameKey(new ImportKeyRequest(gameId, "KEY-123")));
    }

    @Test
    void shouldThrowWhenGameAlreadyOwnedOnImport() {
        User user = User.create("buyer", "mail", "pass", Role.BUYER);
        UUID gameId = UUID.randomUUID();

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, user);
        game.approve();
        ReflectionTestUtils.setField(game, "id", gameId);

        Library library = Library.create(user);
        library.addGame(game, "EXISTING-KEY");

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of()));

        assertThrows(BusinessException.class,
                () -> libraryService.importGameKey(new ImportKeyRequest(gameId, "KEY-456")));
    }
}
