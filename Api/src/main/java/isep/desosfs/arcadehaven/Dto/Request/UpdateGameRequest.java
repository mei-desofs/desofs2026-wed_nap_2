package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateGameRequest(
        @NoHtml String title,
        @NoHtml String description,
        @DecimalMin("0.01") BigDecimal price
) {}
