package isep.desosfs.arcadehaven.Dto.Request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotEmpty List<UUID> gameIds
) {}
