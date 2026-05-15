package isep.desosfs.arcadehaven.Dto.Response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.OrderItem;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class OrderItemResponseTest {
    @Test
    void whenFromOrderItem_thenMappingCorrect() {
        User publisher = User.create("pub", "pub@example.com", "hash", Role.BUYER);
        Game game = Game.create("Game", "Desc", BigDecimal.valueOf(15), null, publisher);
        OrderItem item = OrderItem.of(game, BigDecimal.valueOf(15));

        OrderItemResponse response = OrderItemResponse.from(item);

        assertThat(response.id()).isEqualTo(item.getId());
        assertThat(response.gameId()).isEqualTo(game.getId());
        assertThat(response.gameTitle()).isEqualTo(game.getTitle());
        assertThat(response.price()).isEqualByComparingTo(item.getPrice());
        assertThat(response.activationKey()).isEqualTo(item.getActivationKey());
    }
}
