package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        user = User.create("john", "john@mail.com", "pass", Role.BUYER);
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
}
