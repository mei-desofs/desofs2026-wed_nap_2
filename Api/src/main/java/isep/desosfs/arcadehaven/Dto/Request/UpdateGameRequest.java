package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateGameRequest(
        @NoHtml @Size(max = 100) String title,
        @NoHtml @Size(max = 1000) String description,
        @DecimalMin("0.01") @DecimalMax("9999.99") BigDecimal price,
        @NoHtml @Size(max = 100) String category
) {}
