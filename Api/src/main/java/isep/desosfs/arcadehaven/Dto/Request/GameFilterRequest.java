package isep.desosfs.arcadehaven.Dto.Request;

import isep.desosfs.arcadehaven.Validation.NoHtml;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GameFilterRequest(
        @Size(max = 100) @NoHtml String title,
        @Size(max = 50) @NoHtml String category,
        @DecimalMin("0.01") @DecimalMax("9999.99") BigDecimal minPrice,
        @DecimalMin("0.01") @DecimalMax("9999.99") BigDecimal maxPrice
) {}