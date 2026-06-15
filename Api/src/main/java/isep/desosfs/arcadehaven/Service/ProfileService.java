package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.ChangePasswordRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateProfileRequest;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;
    private final PasswordPolicyService passwordPolicyService;
    private final RestTemplate restTemplate;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    // ASVS V1.2.2
    @PostConstruct
    private void validateServerUrl() {
        URI uri = URI.create(keycloakServerUrl);

        if (uri.getScheme() == null) {
            throw new IllegalStateException("Keycloak server URL missing scheme");
        }

        List<String> allowedSchemes = List.of("http", "https");

        if (!allowedSchemes.contains(uri.getScheme().toLowerCase())) {
            throw new IllegalStateException("Invalid Keycloak URL scheme");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalStateException("Keycloak server URL missing host");
        }
    }

    public ProfileService(UserRepository userRepository, Keycloak keycloak,
                          PasswordPolicyService passwordPolicyService, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
        this.passwordPolicyService = passwordPolicyService;
        this.restTemplate = restTemplate;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        return UserResponse.from(getCurrentUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use");
        }

        user.updateEmail(request.email());
        return UserResponse.from(userRepository.save(user));
    }

    // V6.2.2 + V6.2.3 — change password, requires current password verification
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        verifyCurrentPassword(username, request.currentPassword());
        passwordPolicyService.validate(request.newPassword());
        updateKeycloakPassword(username, request.newPassword());
    }

    private void verifyCurrentPassword(String username, String currentPassword) {
        URI tokenUri = UriComponentsBuilder // ASVS V1.2.2
                .fromUriString(keycloakServerUrl)
                .pathSegment("realms", realm, "protocol", "openid-connect", "token")
                .build()
                .encode()
                .toUri();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", currentPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            restTemplate.postForObject(tokenUri, new HttpEntity<>(form, headers), Map.class);
        } catch (HttpClientErrorException e) {
            throw new BusinessException("Current password is incorrect");
        } catch (Exception e) {
            throw new BusinessException("Authentication service is unavailable. Please try again later.");
        }
    }

    private void updateKeycloakPassword(String username, String newPassword) {
        List<UserRepresentation> users = keycloak.realm(realm).users().searchByUsername(username, true);
        if (users.isEmpty()) {
            throw new BusinessException("User not found in identity provider");
        }
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);
        keycloak.realm(realm).users().get(users.get(0).getId()).resetPassword(credential);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
