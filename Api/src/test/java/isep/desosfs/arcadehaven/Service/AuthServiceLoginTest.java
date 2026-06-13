package isep.desosfs.arcadehaven.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.LoginRequest;
import isep.desosfs.arcadehaven.Dto.Request.RegisterRequest;
import isep.desosfs.arcadehaven.Dto.Response.LoginResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    @Mock private UserRepository userRepository;
    @Mock private LibraryRepository libraryRepository;
    @Mock private Keycloak keycloak;
    @Mock private PasswordPolicyService passwordPolicyService;
    @Mock private RestTemplate restTemplate;
    @Mock private SecurityAuditService securityAuditService;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "realm", "arcadehaven");
        ReflectionTestUtils.setField(authService, "keycloakServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "clientId", "arcade-client");
    }

    @Test
    void login_validCredentials_returnsTokens() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of(
                        "access_token", "acc-token",
                        "refresh_token", "ref-token",
                        "token_type", "Bearer",
                        "expires_in", 3600));

        LoginResponse response = authService.login(new LoginRequest("user", "pass"));

        assertThat(response.accessToken()).isEqualTo("acc-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void login_wrongPassword_throwsBadCredentialsException() {
        HttpClientErrorException.Unauthorized unauthorized =
                mock(HttpClientErrorException.Unauthorized.class);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(unauthorized);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_keycloakDown_throwsBusinessException() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user", "pass")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void register_passwordPolicyViolation_throwsBusinessException() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        doThrow(new BusinessException("Password too weak"))
                .when(passwordPolicyService).validate("weak");

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("john", "john@mail.com", "weak", Role.BUYER)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password too weak");
    }
}
