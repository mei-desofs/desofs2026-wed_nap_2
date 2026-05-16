package isep.desosfs.arcadehaven.Dto.Request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddOrderItemRequest(
        @NotNull UUID gameId
) {}
