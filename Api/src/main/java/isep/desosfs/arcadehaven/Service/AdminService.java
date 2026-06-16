package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.LibraryEntry;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final SecurityAuditService auditService;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public AdminService(UserRepository userRepository, LibraryRepository libraryRepository,
            SecurityAuditService auditService, Keycloak keycloak) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.auditService = auditService;
        this.keycloak = keycloak;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getUserById(UUID id) {
        return UserResponse.from(findUser(id));
    }

    // V7.4.2 — disable user in Keycloak and revoke all active sessions on deactivation
    @Transactional
    public UserResponse deactivateUser(UUID id) {
        User user = findUser(id);
        user.deactivate();
        UserResponse result = UserResponse.from(userRepository.save(user));
        keycloakDisableAndLogout(user.getUsername());
        auditService.recordAdminAction(currentAdminUsername(), "DEACTIVATE_USER", id.toString());
        return result;
    }

    // V7.4.2 — re-enable user in Keycloak when admin reactivates the account
    @Transactional
    public UserResponse activateUser(UUID id) {
        User user = findUser(id);
        user.activate();
        UserResponse result = UserResponse.from(userRepository.save(user));
        keycloakEnable(user.getUsername());
        auditService.recordAdminAction(currentAdminUsername(), "ACTIVATE_USER", id.toString());
        return result;
    }

    // V7.4.5 — revoke all active Keycloak sessions without disabling the account
    public void revokeUserSessions(UUID id) {
        User user = findUser(id);
        List<UserRepresentation> kcUsers =
                keycloak.realm(realm).users().searchByUsername(user.getUsername(), true);
        if (kcUsers.isEmpty()) {
            log.warn("No Keycloak user found for username '{}' when revoking sessions", user.getUsername());
            auditService.recordAdminAction(currentAdminUsername(), "REVOKE_SESSIONS", id.toString());
            return;
        }
        try {
            keycloak.realm(realm).users().get(kcUsers.get(0).getId()).logout();
            log.info("All Keycloak sessions revoked for user '{}' by admin", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to revoke Keycloak sessions for user '{}'", user.getUsername(), e);
            throw new BusinessException("Failed to revoke user sessions. Please try again.");
        }
        auditService.recordAdminAction(currentAdminUsername(), "REVOKE_SESSIONS", id.toString());
    }

    @Transactional
    public UserResponse changeUserRole(UUID id, Role role) {
        User user = findUser(id);
        user.changeRole(role);
        UserResponse result = UserResponse.from(userRepository.save(user));
        auditService.recordAdminAction(currentAdminUsername(), "CHANGE_ROLE:" + role.name(), id.toString());
        return result;
    }

    // RF-30: Suspend a library entry (admin revokes access to a specific game)
    @Transactional
    public void suspendLibraryEntry(UUID userId, UUID entryId) {
        Library library = findLibrary(userId);
        LibraryEntry entry = findEntry(library, entryId);
        entry.suspend();
        libraryRepository.save(library);
        auditService.recordAdminAction(currentAdminUsername(), "SUSPEND_LIBRARY_ENTRY",
                "user=" + userId + ",entry=" + entryId);
    }

    // RF-30: Revoke a library entry (admin permanently removes the game from the library)
    @Transactional
    public void revokeLibraryEntry(UUID userId, UUID entryId) {
        Library library = findLibrary(userId);
        LibraryEntry entry = findEntry(library, entryId);
        entry.refund();
        libraryRepository.save(library);
        auditService.recordAdminAction(currentAdminUsername(), "REVOKE_LIBRARY_ENTRY",
                "user=" + userId + ",entry=" + entryId);
    }

    private String currentAdminUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "system";
    }

    private void keycloakDisableAndLogout(String username) {
        List<UserRepresentation> kcUsers =
                keycloak.realm(realm).users().searchByUsername(username, true);
        if (kcUsers.isEmpty()) {
            log.warn("No Keycloak user found for username '{}' during deactivation", username);
            return;
        }
        try {
            String kcId = kcUsers.get(0).getId();
            UserRepresentation kcUser = kcUsers.get(0);
            kcUser.setEnabled(false);
            keycloak.realm(realm).users().get(kcId).update(kcUser);
            keycloak.realm(realm).users().get(kcId).logout();
            log.info("Keycloak user '{}' disabled and sessions revoked", username);
        } catch (Exception e) {
            log.error("Failed to disable/logout Keycloak user '{}'", username, e);
            throw new BusinessException("Failed to deactivate user in identity provider. Please try again.");
        }
    }

    private void keycloakEnable(String username) {
        List<UserRepresentation> kcUsers =
                keycloak.realm(realm).users().searchByUsername(username, true);
        if (kcUsers.isEmpty()) {
            log.warn("No Keycloak user found for username '{}' during activation", username);
            return;
        }
        try {
            String kcId = kcUsers.get(0).getId();
            UserRepresentation kcUser = kcUsers.get(0);
            kcUser.setEnabled(true);
            keycloak.realm(realm).users().get(kcId).update(kcUser);
            log.info("Keycloak user '{}' re-enabled", username);
        } catch (Exception e) {
            log.error("Failed to re-enable Keycloak user '{}'", username, e);
            throw new BusinessException("Failed to activate user in identity provider. Please try again.");
        }
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Library findLibrary(UUID userId) {
        User user = findUser(userId);
        return libraryRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found for user"));
    }

    private LibraryEntry findEntry(Library library, UUID entryId) {
        return library.getEntries().stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Library entry not found"));
    }
}
