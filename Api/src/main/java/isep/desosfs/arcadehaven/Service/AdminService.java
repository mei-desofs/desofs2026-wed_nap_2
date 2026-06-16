package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.LibraryEntry;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.SecurityAuditService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final SecurityAuditService auditService;

    public AdminService(UserRepository userRepository, LibraryRepository libraryRepository,
            SecurityAuditService auditService) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.auditService = auditService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getUserById(UUID id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse deactivateUser(UUID id) {
        User user = findUser(id);
        user.deactivate();
        UserResponse result = UserResponse.from(userRepository.save(user));
        auditService.recordAdminAction(currentAdminUsername(), "DEACTIVATE_USER", id.toString());
        return result;
    }

    @Transactional
    public UserResponse activateUser(UUID id) {
        User user = findUser(id);
        user.activate();
        UserResponse result = UserResponse.from(userRepository.save(user));
        auditService.recordAdminAction(currentAdminUsername(), "ACTIVATE_USER", id.toString());
        return result;
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
