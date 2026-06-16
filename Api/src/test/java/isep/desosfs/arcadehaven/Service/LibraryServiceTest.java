package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    private static final String VALID_KEY = "1A2B3C4D5E6F7890ABCDEF1234567890";
    private static final String EXISTING_KEY = "1A2B3C4D5E6F7890ABCDEF1234567890";
    private static final String NEW_KEY = "AB12CD34EF56AB78CD90EF12AB34CD56";
    private static final String IMPORT_KEY = "FEDCBA9876543210FEDCBA9876543210";

    @Mock private LibraryRepository libraryRepository;
    @Mock private UserRepository userRepository;
    @Mock private GameRepository gameRepository;

    @InjectMocks
    private LibraryService libraryService;

    private User buyer;
    private Library library;
    private Game activeGame;

    @BeforeEach
    void setUp() {
        buyer = User.create("buyer", "buyer@test.com", "hash", Role.BUYER);
        library = Library.create(buyer);

        User publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        activeGame = Game.create("Test Game", "desc", BigDecimal.TEN, null, null, publisher);
        activeGame.approve();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("buyer");

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

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

        LibraryResponse response = libraryService.importGameKey(new ImportKeyRequest(gameId, VALID_KEY));

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
        library.addGame(game, EXISTING_KEY);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of()));

        assertThrows(BusinessException.class,
                () -> libraryService.importGameKey(new ImportKeyRequest(gameId, NEW_KEY)));
    }

    @Test
    void getMyLibrary_returnsLibraryResponse() {
        when(userRepository.findByUsername("buyer"))
                .thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer))
                .thenReturn(Optional.of(library));

        LibraryResponse response = libraryService.getMyLibrary();

        assertThat(response).isNotNull();
    }

    @Test
    void getMyLibrary_libraryNotFound_throwsResourceNotFound() {
        when(userRepository.findByUsername("buyer"))
                .thenReturn(Optional.of(buyer));

        when(libraryRepository.findByUser(buyer))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> libraryService.getMyLibrary())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Library not found");
    }

    @Test
    void importGameKey_success_addsEntryToLibrary() {
        UUID gameId = activeGame.getId();

        when(userRepository.findByUsername("buyer"))
                .thenReturn(Optional.of(buyer));

        when(libraryRepository.findByUser(buyer))
                .thenReturn(Optional.of(library));

        when(gameRepository.findById(gameId))
                .thenReturn(Optional.of(activeGame));

        when(libraryRepository.save(any(Library.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LibraryResponse response = libraryService.importGameKey(
                new ImportKeyRequest(gameId, IMPORT_KEY));

        assertThat(response.entries()).hasSize(1);
        assertThat(response.entries().get(0).activationKey())
                .isEqualTo(IMPORT_KEY);
    }

    @Test
    void importGameKey_gameNotFound_throwsResourceNotFound() {
        UUID missing = UUID.randomUUID();

        when(userRepository.findByUsername("buyer"))
                .thenReturn(Optional.of(buyer));

        when(libraryRepository.findByUser(buyer))
                .thenReturn(Optional.of(library));

        when(gameRepository.findById(missing))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                libraryService.importGameKey(
                        new ImportKeyRequest(missing, "KEY")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Game not found");
    }

    @Test
    void importGameKey_gameNotActive_throwsBusinessException() {
        User publisher =
                User.create("pub2", "pub2@test.com", "hash", Role.PUBLISHER);

        Game pendingGame =
                Game.create("Pending", "desc", BigDecimal.TEN, null, null, publisher);

        UUID gameId = pendingGame.getId();

        when(userRepository.findByUsername("buyer"))
                .thenReturn(Optional.of(buyer));

        when(libraryRepository.findByUser(buyer))
                .thenReturn(Optional.of(library));

        when(gameRepository.findById(gameId))
                .thenReturn(Optional.of(pendingGame));

        assertThatThrownBy(() ->
                libraryService.importGameKey(
                        new ImportKeyRequest(gameId, "KEY")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Game is not available");
    }

//    @Test
//    void importGameKey_alreadyOwned_throwsBusinessException() {
//        library.addGame(activeGame, "EXISTING");
//        UUID gameId = activeGame.getId();
//        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));
//
//        assertThatThrownBy(() -> libraryService.importGameKey(
//                new ImportKeyRequest(gameId, "NEW-KEY")))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining("You already own this game");
//    }

//    @Test
//    void importGameKey_userNotFound_throwsResourceNotFound() {
//        when(userRepository.findByUsername("buyer")).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> libraryService.importGameKey(
//                new ImportKeyRequest(UUID.randomUUID(), "KEY")))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("User not found");
//    }
}