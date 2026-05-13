package isep.desosfs.arcadehaven.Config;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;

    @Value("${admin.default.username:admin}")
    private String defaultUsername;

    @Value("${admin.default.email:admin@arcadehaven.com}")
    private String defaultEmail;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(defaultUsername)) {
            return;
        }
        // Auth is managed by Keycloak — passwordHash is not stored locally
        User admin = User.create(defaultUsername, defaultEmail, "", Role.ADMIN);
        userRepository.save(admin);
        log.info("Admin local profile created: '{}' — account managed by Keycloak", defaultUsername);
    }
}
