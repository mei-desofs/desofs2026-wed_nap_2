package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.OrderStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class OrderTest {
    @Test
    void shouldCreateOrder() {
        User buyer = createUser();

        Order order = Order.create(buyer);

        assertEquals(buyer, order.getBuyer());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldAddItem() {
        Order order = Order.create(createUser());

        order.addItem(createItem(BigDecimal.TEN));

        assertEquals(1, order.getItems().size());
    }

    @Test
    void shouldCalculateTotal() {
        Order order = Order.create(createUser());

        order.addItem(createItem(BigDecimal.TEN));
        order.addItem(createItem(BigDecimal.valueOf(20)));

        BigDecimal total = order.calculateTotal();

        assertEquals(BigDecimal.valueOf(30), total);
    }

    @Test
    void shouldCompleteOrder() {
        Order order = Order.create(createUser());

        OrderItem item = createItem(BigDecimal.TEN);

        order.addItem(item);

        order.complete("/invoice.pdf");

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals("/invoice.pdf", order.getInvoicePath());
        assertNotNull(item.getActivationKey());
    }

    @Test
    void shouldThrowWhenCompletingNonPendingOrder() {
        Order order = Order.create(createUser());

        order.complete("/invoice.pdf");

        assertThrows(
                IllegalStateException.class,
                () -> order.complete("/invoice2.pdf")
        );
    }

    @Test
    void shouldCancelOrder() {
        Order order = Order.create(createUser());

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    private User createUser() {
        return User.create(
                "buyer",
                "buyer@test.com",
                "hash",
                Role.BUYER
        );
    }

    private OrderItem createItem(BigDecimal price) {
        Game game = Game.create(
                "Game",
                "Desc",
                price,
                "rawg",
                createUser()
        );

        return OrderItem.of(game, price);
    }
}
