package isep.desosfs.arcadehaven.Config;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private record SeedUser(String username, String email, Role role) {}

    private static final List<SeedUser> DEFAULT_USERS = List.of(
        new SeedUser("publisher1", "publisher1@arcadehaven.com", Role.PUBLISHER),
        new SeedUser("buyer1",     "buyer1@arcadehaven.com",     Role.BUYER)
    );

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;

    @Value("${admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${admin.default.email:admin@arcadehaven.com}")
    private String defaultAdminEmail;

    public AdminInitializer(UserRepository userRepository, LibraryRepository libraryRepository) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            seedUser(defaultAdminUsername, defaultAdminEmail, Role.ADMIN);
            DEFAULT_USERS.forEach(u -> seedUser(u.username(), u.email(), u.role()));
        } catch (Exception e) {
            log.error("Failed to seed default users — application will continue without them", e);
        }
    }

    private void seedUser(String username, String email, Role role) {
        try {
            if (userRepository.existsByUsername(username)) {
                return;
            }
            User user = User.create(username, email, "", role);
            userRepository.save(user);
            if (role == Role.BUYER) {
                libraryRepository.save(Library.create(user));
            }
            log.info("Default user created: '{}' [{}]", username, role);
        } catch (Exception e) {
            log.error("Failed to seed user '{}': {}", username, e.getMessage());
        }
    }
}
