package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Response.LibraryResponse;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {
    @Mock private LibraryRepository libraryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private LibraryService libraryService;

    @Test
    void shouldReturnLibrary() {
        User user = User.create("buyer", "mail", "pass", Role.BUYER);
        Library library = Library.create(user);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(libraryRepository.findByUser(user)).thenReturn(Optional.of(library));

        var auth = new UsernamePasswordAuthenticationToken(
            "buyer",
            null,
            List.of()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        LibraryResponse response = libraryService.getMyLibrary();

        assertNotNull(response);
    }
}
