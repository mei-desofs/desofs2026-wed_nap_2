package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NoHtml @NotBlank String username,
        @NoHtml @NotBlank String password
) {}
