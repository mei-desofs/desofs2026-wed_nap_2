package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Dto.Request.UpdateProfileRequest;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Service.ProfileService;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock ProfileService profileService;
    @InjectMocks ProfileController controller;

    @Test
    void shouldGetProfile() {
        UserResponse user = createUserResponse();
        when(profileService.getProfile()).thenReturn(user);

        var response = controller.getProfile();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
        verify(profileService).getProfile();
    }

    @Test
    void shouldUpdateProfile() {
        UpdateProfileRequest req = new UpdateProfileRequest("new@example.com");
        UserResponse user = createUserResponse();

        when(profileService.updateProfile(req)).thenReturn(user);

        var response = controller.updateProfile(req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
        verify(profileService).updateProfile(req);
    }

    private UserResponse createUserResponse() {
        return new UserResponse(
                UUID.randomUUID(),
                "alice",
                "alice@example.com",
                "BUYER",
                true,
                LocalDateTime.now()
        );
    }
}
