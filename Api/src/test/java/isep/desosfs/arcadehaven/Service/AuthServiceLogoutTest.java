package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService.logout() — ASVS V6.8.3 / RNF-03.
 * Verifies that all Keycloak sessions are revoked on logout and that
 * edge cases (unknown user, Keycloak error) are handled safely.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceLogoutTest {

    @Mock private UserRepository userRepository;
    @Mock private LibraryRepository libraryRepository;
    @Mock private Keycloak keycloak;
    @Mock private PasswordPolicyService passwordPolicyService;
    @Mock private SecurityAuditService securityAuditService;

    @InjectMocks
    private AuthService authService;

    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "realm", "arcadehaven");
    }

    // ── Happy path ───────────────────────────────────────────────────────────────

    @Test
    void logout_knownUser_revokesAllKeycloakSessions() {
        UserRepresentation user = new UserRepresentation();
        user.setId("kc-id-buyer1");

        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("buyer1", true)).thenReturn(List.of(user));
        when(usersResource.get("kc-id-buyer1")).thenReturn(userResource);

        authService.logout("buyer1");

        verify(userResource).logout();
    }

    @Test
    void logout_knownUser_completesWithoutException() {
        UserRepresentation user = new UserRepresentation();
        user.setId("kc-id-buyer1");

        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("buyer1", true)).thenReturn(List.of(user));
        when(usersResource.get("kc-id-buyer1")).thenReturn(userResource);

        assertThatCode(() -> authService.logout("buyer1")).doesNotThrowAnyException();
    }

    // ── Unknown user ─────────────────────────────────────────────────────────────

    @Test
    void logout_unknownUser_doesNotThrow() {
        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("ghost", true)).thenReturn(List.of());

        assertThatCode(() -> authService.logout("ghost")).doesNotThrowAnyException();
    }

    @Test
    void logout_unknownUser_neverCallsUserLogout() {
        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("ghost", true)).thenReturn(List.of());

        authService.logout("ghost");

        verify(usersResource, never()).get(anyString());
    }

    // ── Error handling ───────────────────────────────────────────────────────────

    @Test
    void logout_keycloakUnavailable_throwsBusinessException() {
        when(keycloak.realm("arcadehaven")).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> authService.logout("buyer1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Logout failed");
    }

    @Test
    void logout_keycloakSessionRevocationFails_throwsBusinessException() {
        UserRepresentation user = new UserRepresentation();
        user.setId("kc-id-buyer1");

        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("buyer1", true)).thenReturn(List.of(user));
        when(usersResource.get("kc-id-buyer1")).thenReturn(userResource);
        org.mockito.Mockito.doThrow(new RuntimeException("Session store error")).when(userResource).logout();

        assertThatThrownBy(() -> authService.logout("buyer1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Logout failed");
    }

    @Test
    void logout_errorMessage_doesNotLeakInternalDetails() {
        when(keycloak.realm("arcadehaven")).thenThrow(new RuntimeException("internal db error details"));

        assertThatThrownBy(() -> authService.logout("buyer1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageNotContaining("internal db error details");
    }
}
