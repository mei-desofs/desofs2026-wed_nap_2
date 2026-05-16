package isep.desosfs.arcadehaven.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ImportKeyRequest(
        @NotNull UUID gameId,
        @NotBlank @Size(min = 1, max = 100) String activationKey
) {}
