package isep.desosfs.arcadehaven.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminInitiliazerTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() throws Exception {

        Field usernameField =
                AdminInitializer.class.getDeclaredField("defaultAdminUsername");

        usernameField.setAccessible(true);
        usernameField.set(adminInitializer, "admin");

        Field emailField =
                AdminInitializer.class.getDeclaredField("defaultAdminEmail");

        emailField.setAccessible(true);
        emailField.set(adminInitializer, "admin@arcadehaven.com");
    }

    @Test
    void shouldCreateAdminAndDefaultUsers() {

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        adminInitializer.run(applicationArguments);

        verify(userRepository, times(3))
                .save(any(User.class));

        verify(libraryRepository, times(1))
                .save(any(Library.class));
    }

    @Test
    void shouldNotCreateUsersWhenTheyAlreadyExist() {

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true);

        adminInitializer.run(applicationArguments);

        verify(userRepository, never())
                .save(any(User.class));

        verify(libraryRepository, never())
                .save(any(Library.class));
    }

    @Test
    void shouldCreateLibraryOnlyForBuyer() {

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        adminInitializer.run(applicationArguments);

        verify(libraryRepository, times(1))
                .save(any(Library.class));
    }

    @Test
    void shouldHandleExceptionWhenCheckingExistingUser() {

        when(userRepository.existsByUsername(anyString()))
                .thenThrow(new RuntimeException("Database failure"));

        adminInitializer.run(applicationArguments);

        verify(userRepository, never())
                .save(any(User.class));

        verify(libraryRepository, never())
                .save(any(Library.class));
    }

    @Test
    void shouldHandleExceptionWhenSavingUser() {

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Save failed"));

        adminInitializer.run(applicationArguments);

        verify(userRepository, atLeastOnce())
                .save(any(User.class));
    }
}
