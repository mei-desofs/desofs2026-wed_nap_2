package isep.desosfs.arcadehaven.Dto.Response;

import isep.desosfs.arcadehaven.Domain.Game;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        String status,
        String rawgApiId,
        String publisherUsername,
        LocalDateTime createdAt
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getPrice(),
                game.getStatus().name(),
                game.getRawgApiId(),
                game.getPublisher().getUsername(),
                game.getCreatedAt()
        );
    }
}
