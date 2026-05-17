package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NoHtml @NotBlank @Size(min = 1, max = 50) String username,
        @NoHtml @NotBlank @Size(min = 1, max = 128) String password
) {}
