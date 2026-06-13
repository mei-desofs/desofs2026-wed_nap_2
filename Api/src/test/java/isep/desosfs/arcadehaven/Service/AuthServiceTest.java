package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.RegisterRequest;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private Keycloak keycloak;

    @Mock
    private SecurityAuditService securityAuditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRejectAdminRegistration() {
        RegisterRequest request =
                new RegisterRequest("john", "mail@mail.com", "password", Role.ADMIN);

        assertThrows(BusinessException.class,
                () -> authService.register(request));
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest request =
                new RegisterRequest("john", "mail@mail.com", "password", Role.BUYER);

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> authService.register(request));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request =
                new RegisterRequest("john", "mail@mail.com", "password", Role.BUYER);

        when(userRepository.existsByEmail("mail@mail.com")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> authService.register(request));
    }
}
