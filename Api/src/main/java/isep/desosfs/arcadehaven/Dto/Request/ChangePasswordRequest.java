package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NoHtml @NotBlank String currentPassword,
        @NoHtml @NotBlank @Size(min = 12, max = 128) String newPassword
) {}
