package isep.desosfs.arcadehaven.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.RegisterRequest;
import isep.desosfs.arcadehaven.Dto.Response.RegisterResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock private UserRepository userRepository;
    @Mock private LibraryRepository libraryRepository;
    @Mock private Keycloak keycloak;
    @Mock private PasswordPolicyService passwordPolicyService;
    @Mock private RestTemplate restTemplate;
    @Mock private SecurityAuditService securityAuditService;

    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;
    @Mock private RolesResource rolesResource;
    @Mock private RoleResource roleResource;
    @Mock private RoleMappingResource roleMappingResource;
    @Mock private RoleScopeResource roleScopeResource;

    @InjectMocks private AuthService authService;

    private static final String REALM = "arcadehaven";
    private static final String KEYCLOAK_USER_ID = "kc-user-123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "realm", REALM);
        ReflectionTestUtils.setField(authService, "keycloakServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "clientId", "arcade-client");
    }

    private void stubKeycloakCreate(int status) throws Exception {
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(status);
        if (status == 201) {
            when(mockResponse.getStatusInfo()).thenReturn(Response.Status.CREATED);
            when(mockResponse.getLocation())
                    .thenReturn(new URI("http://localhost/admin/realms/arcadehaven/users/" + KEYCLOAK_USER_ID));
        } else {
            when(mockResponse.getStatus()).thenReturn(status);
        }
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
    }

    private void stubKeycloakRoleAssign() {
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName("BUYER");

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("BUYER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRep);
        when(usersResource.get(KEYCLOAK_USER_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    }

    @Test
    void register_happyPath_createsUserAndReturnsResponse() throws Exception {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        stubKeycloakCreate(201);
        stubKeycloakRoleAssign();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(libraryRepository.save(any(Library.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse response = authService.register(
                new RegisterRequest("john", "john@mail.com", "Str0ngP@ss!", Role.BUYER));

        assertThat(response.username()).isEqualTo("john");
        assertThat(response.role()).isEqualTo("BUYER");
        verify(userRepository).save(any(User.class));
        verify(libraryRepository).save(any(Library.class));
        verify(securityAuditService).recordRegistrationSuccess("john");
    }

    @Test
    void register_keycloakReturns409_throwsBusinessException() throws Exception {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        stubKeycloakCreate(409);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("john", "john@mail.com", "Str0ngP@ss!", Role.BUYER)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to create user in Keycloak");
    }

    @Test
    void register_keycloakConnectionError_throwsBusinessException() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        when(keycloak.realm(REALM)).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("john", "john@mail.com", "Str0ngP@ss!", Role.BUYER)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Identity provider is unavailable");
    }

    @Test
    void register_roleNotFoundInKeycloak_throwsBusinessException() throws Exception {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        stubKeycloakCreate(201);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("BUYER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(null);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("john", "john@mail.com", "Str0ngP@ss!", Role.BUYER)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not exist in Keycloak");
    }

    @Test
    void register_publisherRole_happyPath() throws Exception {
        when(userRepository.existsByUsername("publisher1")).thenReturn(false);
        when(userRepository.existsByEmail("pub@mail.com")).thenReturn(false);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);
        when(mockResponse.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(mockResponse.getLocation())
                .thenReturn(new URI("http://localhost/admin/realms/arcadehaven/users/" + KEYCLOAK_USER_ID));
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName("PUBLISHER");
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("PUBLISHER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRep);
        when(usersResource.get(KEYCLOAK_USER_ID)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(libraryRepository.save(any(Library.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse response = authService.register(
                new RegisterRequest("publisher1", "pub@mail.com", "Str0ngP@ss!", Role.PUBLISHER));

        assertThat(response.username()).isEqualTo("publisher1");
        assertThat(response.role()).isEqualTo("PUBLISHER");
    }
}
