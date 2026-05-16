package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.ImportKeyRequest;
import isep.desosfs.arcadehaven.Dto.Response.LibraryResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

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
        activeGame = Game.create("Test Game", "desc", BigDecimal.TEN, null, publisher);
        activeGame.approve();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("buyer");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
    }

    @Test
    void getMyLibrary_returnsLibraryResponse() {
        LibraryResponse response = libraryService.getMyLibrary();

        assertThat(response).isNotNull();
        assertThat(response.entries()).isEmpty();
    }

    @Test
    void getMyLibrary_libraryNotFound_throwsResourceNotFound() {
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libraryService.getMyLibrary())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Library not found");
    }

    @Test
    void importGameKey_success_addsEntryToLibrary() {
        UUID gameId = activeGame.getId();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));
        when(libraryRepository.save(any(Library.class))).thenAnswer(inv -> inv.getArgument(0));

        LibraryResponse response = libraryService.importGameKey(
                new ImportKeyRequest(gameId, "MY-KEY-123"));

        assertThat(response.entries()).hasSize(1);
        assertThat(response.entries().get(0).activationKey()).isEqualTo("MY-KEY-123");
    }

    @Test
    void importGameKey_gameNotFound_throwsResourceNotFound() {
        UUID missing = UUID.randomUUID();
        when(gameRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libraryService.importGameKey(
                new ImportKeyRequest(missing, "KEY")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Game not found");
    }

    @Test
    void importGameKey_gameNotActive_throwsBusinessException() {
        User publisher = User.create("pub2", "pub2@test.com", "hash", Role.PUBLISHER);
        Game pendingGame = Game.create("Pending", "desc", BigDecimal.TEN, null, publisher);
        UUID gameId = pendingGame.getId();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(pendingGame));

        assertThatThrownBy(() -> libraryService.importGameKey(
                new ImportKeyRequest(gameId, "KEY")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Game is not available");
    }

    @Test
    void importGameKey_alreadyOwned_throwsBusinessException() {
        library.addGame(activeGame, "EXISTING");
        UUID gameId = activeGame.getId();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));

        assertThatThrownBy(() -> libraryService.importGameKey(
                new ImportKeyRequest(gameId, "NEW-KEY")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You already own this game");
    }

    @Test
    void importGameKey_userNotFound_throwsResourceNotFound() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libraryService.importGameKey(
                new ImportKeyRequest(UUID.randomUUID(), "KEY")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
