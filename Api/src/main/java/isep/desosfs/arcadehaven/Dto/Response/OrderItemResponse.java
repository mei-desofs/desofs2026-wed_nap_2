package isep.desosfs.arcadehaven.Dto.Response;

import isep.desosfs.arcadehaven.Domain.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID gameId,
        String gameTitle,
        BigDecimal price,
        String activationKey
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getGame().getId(),
                item.getGame().getTitle(),
                item.getPrice(),
                item.getActivationKey()
        );
    }
}
