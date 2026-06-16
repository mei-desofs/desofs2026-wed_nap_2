package isep.desosfs.arcadehaven.Service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import isep.desosfs.arcadehaven.Dto.Request.ChangePasswordRequest;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceChangePasswordTest {

    @Mock private UserRepository userRepository;
    @Mock private Keycloak keycloak;
    @Mock private PasswordPolicyService passwordPolicyService;
    @Mock private RestTemplate restTemplate;

    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;

    @InjectMocks private ProfileService profileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileService, "realm", "arcadehaven");
        ReflectionTestUtils.setField(profileService, "keycloakServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(profileService, "clientId", "arcade-client");
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changePassword_happyPath_resetsKeycloakCredential() {
        when(restTemplate.postForObject(any(), any(), eq(Map.class))).thenReturn(Map.of());

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId("kc-alice-id");
        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("alice", true)).thenReturn(List.of(kcUser));
        when(usersResource.get("kc-alice-id")).thenReturn(userResource);

        assertThatCode(() -> profileService.changePassword(
                new ChangePasswordRequest("OldPass1!", "NewStr0ng!Pass")))
                .doesNotThrowAnyException();

        verify(userResource).resetPassword(any());
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBusinessException() {
        when(restTemplate.postForObject(any(), any(), eq(Map.class)))
                .thenThrow(mock(HttpClientErrorException.Unauthorized.class));

        assertThatThrownBy(() -> profileService.changePassword(
                new ChangePasswordRequest("wrongPass", "NewStr0ng!Pass")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void changePassword_keycloakUnavailableOnVerify_throwsBusinessException() {
        when(restTemplate.postForObject(any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> profileService.changePassword(
                new ChangePasswordRequest("pass", "NewStr0ng!Pass")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Authentication service is unavailable");
    }

    @Test
    void changePassword_userNotFoundInKeycloak_throwsBusinessException() {
        when(restTemplate.postForObject(any(), any(), eq(Map.class))).thenReturn(Map.of());
        when(keycloak.realm("arcadehaven")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("alice", true)).thenReturn(List.of());

        assertThatThrownBy(() -> profileService.changePassword(
                new ChangePasswordRequest("OldPass1!", "NewStr0ng!Pass")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found in identity provider");
    }
}
