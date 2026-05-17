package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.LoginRequest;
import isep.desosfs.arcadehaven.Dto.Request.RegisterRequest;
import isep.desosfs.arcadehaven.Dto.Response.LoginResponse;
import isep.desosfs.arcadehaven.Dto.Response.RegisterResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final Keycloak keycloak;
    private final PasswordPolicyService passwordPolicyService;
    private final RestTemplate restTemplate;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    public AuthService(UserRepository userRepository, LibraryRepository libraryRepository,
                       Keycloak keycloak, PasswordPolicyService passwordPolicyService,
                       RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.keycloak = keycloak;
        this.passwordPolicyService = passwordPolicyService;
        this.restTemplate = restTemplate;
    }

    public LoginResponse login(LoginRequest request) {
        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", request.username());
        form.add("password", request.password());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.postForObject(
                    tokenUrl, new HttpEntity<>(form, headers), Map.class);
            return new LoginResponse(
                    (String) body.get("access_token"),
                    (String) body.get("refresh_token"),
                    (String) body.get("token_type"),
                    ((Number) body.get("expires_in")).longValue()
            );
        } catch (HttpClientErrorException e) {
            // Keycloak returns 4xx (401 invalid credentials, 400 account not set up, etc.)
            // — all map to authentication failure from the client's perspective
            log.warn("Keycloak rejected login for user '{}': {} {}", request.username(),
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("Login failed for user '{}'", request.username(), e);
            throw new BusinessException("Authentication service is unavailable. Please try again later.");
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new BusinessException("Invalid role for registration");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered");
        }

        passwordPolicyService.validate(request.password());

        String keycloakUserId = createKeycloakUser(request);
        assignKeycloakRole(keycloakUserId, request.role().name());

        User user = User.create(request.username(), request.email(), "", request.role());
        userRepository.save(user);

        libraryRepository.save(Library.create(user));

        return new RegisterResponse(user.getUsername(), user.getRole().name());
    }

    /**
     * Revokes all active Keycloak sessions for the current user (ASVS V6.8.3 / V10.4.9 / RNF-03).
     * Called from POST /api/auth/logout.
     */
    public void logout(String username) {
        try {
            java.util.List<UserRepresentation> users =
                    keycloak.realm(realm).users().searchByUsername(username, true);
            if (users.isEmpty()) {
                log.warn("Logout requested for unknown Keycloak user: {}", username);
                return;
            }
            keycloak.realm(realm).users().get(users.get(0).getId()).logout();
            log.info("All Keycloak sessions revoked for user '{}'", username);
        } catch (Exception e) {
            log.error("Failed to revoke Keycloak sessions for user '{}'", username, e);
            throw new BusinessException("Logout failed. Please try again.");
        }
    }

    private String createKeycloakUser(RegisterRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.username());
        kcUser.setEmail(request.email());
        kcUser.setFirstName(request.username());
        kcUser.setLastName("User");
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        kcUser.setRequiredActions(List.of());
        kcUser.setCredentials(List.of(credential));

        try (Response response = keycloak.realm(realm).users().create(kcUser)) {
            if (response.getStatus() != 201) {
                log.error("Keycloak user creation failed with status {}", response.getStatus());
                throw new BusinessException("Failed to create user in Keycloak (status " + response.getStatus() + ")");
            }
            return CreatedResponseUtil.getCreatedId(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak connection error during user creation", e);
            throw new BusinessException("Identity provider is unavailable. Please try again later.");
        }
    }

    private void assignKeycloakRole(String keycloakUserId, String roleName) {
        try {
            RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
            if (role == null) {
                throw new BusinessException("Role '" + roleName + "' does not exist in Keycloak");
            }
            keycloak.realm(realm).users().get(keycloakUserId).roles().realmLevel().add(List.of(role));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to assign role '{}' to Keycloak user '{}'", roleName, keycloakUserId, e);
            throw new BusinessException("Failed to assign role. Please try again later.");
        }
    }
}
