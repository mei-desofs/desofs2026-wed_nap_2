package isep.desosfs.arcadehaven.Dto.Response;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

public class GameResponseTest {
    @Test
    void whenFromGame_thenMappingCorrect() {
        User publisher = User.create("publisher", "pub@example.com", "hash", Role.BUYER);
        Game game = Game.create("Game", "Desc", BigDecimal.valueOf(20), "RAWG123", publisher);

        GameResponse response = GameResponse.from(game);

        assertThat(response.id()).isEqualTo(game.getId());
        assertThat(response.title()).isEqualTo(game.getTitle());
        assertThat(response.description()).isEqualTo(game.getDescription());
        assertThat(response.price()).isEqualByComparingTo(game.getPrice());
        assertThat(response.status()).isEqualTo(game.getStatus().name());
        assertThat(response.rawgApiId()).isEqualTo(game.getRawgApiId());
        assertThat(response.publisherUsername()).isEqualTo(game.getPublisher().getUsername());
        assertThat(response.createdAt()).isEqualTo(game.getCreatedAt());
    }
}
