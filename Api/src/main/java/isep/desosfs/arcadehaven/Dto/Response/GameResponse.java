package isep.desosfs.arcadehaven.Dto.Response;

import isep.desosfs.arcadehaven.Domain.Game;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        String status,
        String rawgApiId,
        String category,
        String coverImageUrl,
        String publisherUsername,
        LocalDateTime createdAt,
        List<GameFileResponse> files
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getPrice(),
                game.getStatus().name(),
                game.getRawgApiId(),
                game.getCategory(),
                game.getCoverImageUrl(),
                game.getPublisher().getUsername(),
                game.getCreatedAt(),
                game.getFiles().stream().map(GameFileResponse::from).toList()
        );
    }
}
