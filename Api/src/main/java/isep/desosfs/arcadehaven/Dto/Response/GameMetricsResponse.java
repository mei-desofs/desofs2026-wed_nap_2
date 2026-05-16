package isep.desosfs.arcadehaven.Dto.Response;

import java.math.BigDecimal;
import java.util.UUID;

public record GameMetricsResponse(
        UUID gameId,
        String gameTitle,
        long unitsSold,
        BigDecimal totalRevenue
) {}
