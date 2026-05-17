package isep.desosfs.arcadehaven.Dto.Response;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Domain.OrderItem;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderResponseTest {
    @Test
    void whenFromOrder_thenMappingCorrect() {
        User buyer = User.create("buyer", "buyer@example.com", "hash", Role.BUYER);
        Order order = Order.create(buyer);

        User publisher = User.create("pub", "pub@example.com", "hash", Role.PUBLISHER);
        Game game1 = Game.create("Game1", "Desc1", BigDecimal.valueOf(10), null, null, publisher);
        Game game2 = Game.create("Game2", "Desc2", BigDecimal.valueOf(20), null, null, publisher);

        OrderItem item1 = OrderItem.of(game1, BigDecimal.valueOf(10));
        OrderItem item2 = OrderItem.of(game2, BigDecimal.valueOf(20));
        order.addItem(item1);
        order.addItem(item2);

        OrderResponse response = OrderResponse.from(order);

        assertThat(response.id()).isEqualTo(order.getId());
        assertThat(response.status()).isEqualTo(order.getStatus().name());
        //assertThat(response.totalPrice()).isEqualByComparingTo(order.calculateTotal());
        assertThat(response.items()).hasSize(2);
        assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());
    }
}
