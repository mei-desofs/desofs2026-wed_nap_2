package isep.desosfs.arcadehaven.Dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateGameRequest(
        @NotBlank String title,
        String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        String rawgApiId
) {}
