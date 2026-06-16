package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.math.BigDecimal;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.LibraryEntry;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    private static final String VALID_ACTIVATION_KEY = "1A2B3C4D5E6F7890ABCDEF1234567890";

    @Mock
    private UserRepository userRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private SecurityAuditService securityAuditService;

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        user = User.create("john", "john@mail.com", "pass", Role.BUYER);
        ReflectionTestUtils.setField(adminService, "realm", "test-realm");
        lenient().when(keycloak.realm(anyString())).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
        lenient().when(usersResource.searchByUsername(anyString(), anyBoolean())).thenReturn(List.of());
    }

    @Test
    void shouldGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = adminService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = adminService.getUserById(id);

        assertNotNull(response);
        verify(userRepository).findById(id);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.getUserById(id));
    }

    @Test
    void shouldDeactivateUser() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        adminService.deactivateUser(id);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void shouldActivateUser() {
        user.deactivate();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        adminService.activateUser(id);

        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void shouldChangeUserRole() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        adminService.changeUserRole(id, Role.PUBLISHER);

        assertEquals(Role.PUBLISHER, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void shouldNotChangeAdminRole() {
        User admin = User.create("admin", "a@mail.com", "pass", Role.ADMIN);

        when(userRepository.findById(id)).thenReturn(Optional.of(admin));

        assertThrows(IllegalStateException.class,
                () -> adminService.changeUserRole(id, Role.BUYER));
    }

    @Test
    void shouldPersistDeactivateFlow() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserResponse response = adminService.deactivateUser(id);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldSuspendLibraryEntry() {
        UUID entryId = UUID.randomUUID();

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, user);
        Library library = Library.create(user);
        library.addGame(game, VALID_ACTIVATION_KEY);

        LibraryEntry entry = library.getEntries().get(0);
        ReflectionTestUtils.setField(entry, "id", entryId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));

        adminService.suspendLibraryEntry(id, entryId);

        verify(libraryRepository).save(library);
    }

    @Test
    void shouldRevokeLibraryEntry() {
        UUID entryId = UUID.randomUUID();

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, user);
        Library library = Library.create(user);
        library.addGame(game, VALID_ACTIVATION_KEY);

        LibraryEntry entry = library.getEntries().get(0);
        ReflectionTestUtils.setField(entry, "id", entryId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));

        adminService.revokeLibraryEntry(id, entryId);

        verify(libraryRepository).save(library);
    }

    @Test
    void shouldThrowWhenLibraryNotFound() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.suspendLibraryEntry(id, UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenEntryNotFoundInLibrary() {
        Library library = Library.create(user);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.suspendLibraryEntry(id, UUID.randomUUID()));
    }
}