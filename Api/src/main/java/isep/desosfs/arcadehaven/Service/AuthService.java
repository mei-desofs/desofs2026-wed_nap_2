package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.RegisterRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public AuthService(UserRepository userRepository, LibraryRepository libraryRepository,
                       Keycloak keycloak) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.keycloak = keycloak;
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

        String keycloakUserId = createKeycloakUser(request);
        assignKeycloakRole(keycloakUserId, request.role().name());

        User user = User.create(request.username(), request.email(), "", request.role());
        userRepository.save(user);

        libraryRepository.save(Library.create(user));

        return new RegisterResponse(user.getUsername(), user.getRole().name());
    }

    private String createKeycloakUser(RegisterRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.username());
        kcUser.setEmail(request.email());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        kcUser.setCredentials(List.of(credential));

        try (Response response = keycloak.realm(realm).users().create(kcUser)) {
            if (response.getStatus() != 201) {
                throw new BusinessException("Failed to create user in Keycloak: " + response.getStatus());
            }
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    private void assignKeycloakRole(String keycloakUserId, String roleName) {
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(keycloakUserId).roles().realmLevel().add(List.of(role));
    }
}
