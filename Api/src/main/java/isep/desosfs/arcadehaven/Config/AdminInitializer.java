package isep.desosfs.arcadehaven.Config;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username:admin}")
    private String defaultUsername;

    @Value("${admin.default.email:admin@arcadehaven.com}")
    private String defaultEmail;

    @Value("${admin.default.password:Admin123!}")
    private String defaultPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(defaultUsername)) {
            return;
        }
        User admin = User.create(defaultUsername, defaultEmail, passwordEncoder.encode(defaultPassword), Role.ADMIN);
        userRepository.save(admin);
        log.info("Default admin account created — username: '{}' | Change the password after first login!", defaultUsername);
    }
}
