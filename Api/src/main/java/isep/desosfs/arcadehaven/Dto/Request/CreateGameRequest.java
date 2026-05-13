package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateGameRequest(
        @NoHtml @NotBlank String title,
        @NoHtml String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NoHtml String rawgApiId
) {}
