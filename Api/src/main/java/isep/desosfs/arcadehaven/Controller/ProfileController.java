package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Dto.Request.UpdateProfileRequest;
import isep.desosfs.arcadehaven.Dto.Response.UserResponse;
import isep.desosfs.arcadehaven.Service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }
}
