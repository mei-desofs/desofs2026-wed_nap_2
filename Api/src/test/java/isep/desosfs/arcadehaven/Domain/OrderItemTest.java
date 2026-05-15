package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class OrderItemTest {
    @Test
    void shouldCreateOrderItem() {
        Game game = createGame();

        OrderItem item = OrderItem.of(game, BigDecimal.TEN);

        assertEquals(game, item.getGame());
        assertEquals(BigDecimal.TEN, item.getPrice());
    }

    @Test
    void shouldGenerateActivationKey() {
        OrderItem item = OrderItem.of(createGame(), BigDecimal.TEN);

        item.generateActivationKey();

        assertNotNull(item.getActivationKey());
        assertFalse(item.getActivationKey().isBlank());
    }

    private Game createGame() {
        User publisher = User.create(
                "publisher",
                "publisher@test.com",
                "hash",
                Role.PUBLISHER
        );

        return Game.create(
                "Game",
                "Desc",
                BigDecimal.TEN,
                "rawg",
                publisher
        );
    }
}
