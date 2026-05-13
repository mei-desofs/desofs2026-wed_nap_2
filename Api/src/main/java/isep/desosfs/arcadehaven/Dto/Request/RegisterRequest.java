package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NoHtml @NotBlank @Size(min = 3, max = 50) String username,
        @NoHtml @NotBlank @Email String email,
        @NoHtml @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role
) {}
