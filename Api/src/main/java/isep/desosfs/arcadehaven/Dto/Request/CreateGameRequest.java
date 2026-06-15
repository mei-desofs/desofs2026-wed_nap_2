package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateGameRequest(
        @NoHtml @NotBlank @Size(max=100) String title,
        @NoHtml @Size(max=1000) String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("9999.99") BigDecimal price,
        @NoHtml @Size(max=50) String rawgApiId,
        @NoHtml @Size(max=100) String category
) {}
