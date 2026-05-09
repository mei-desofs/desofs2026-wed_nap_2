package isep.desosfs.arcadehaven.Dto.Request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateGameRequest(
        String title,
        String description,
        @DecimalMin("0.01") BigDecimal price
) {}
