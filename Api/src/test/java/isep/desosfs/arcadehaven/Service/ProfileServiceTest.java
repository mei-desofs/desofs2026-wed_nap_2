package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.UpdateProfileRequest;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks ProfileService profileService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.create("alice", "alice@example.com", "pass", Role.BUYER);
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetProfile() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserResponse response = profileService.getProfile();

        assertNotNull(response);
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void shouldThrowWhenUserNotFoundOnGetProfile() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfile());
    }

    @Test
    void shouldUpdateProfileWithSameEmail() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateProfileRequest req = new UpdateProfileRequest("alice@example.com");
        UserResponse response = profileService.updateProfile(req);

        assertNotNull(response);
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateProfileWithNewAvailableEmail() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        profileService.updateProfile(new UpdateProfileRequest("new@example.com"));

        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    void shouldThrowWhenEmailAlreadyInUse() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> profileService.updateProfile(new UpdateProfileRequest("taken@example.com")));
    }

    @Test
    void shouldThrowWhenUserNotFoundOnUpdateProfile() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.updateProfile(new UpdateProfileRequest("x@example.com")));
    }
}
