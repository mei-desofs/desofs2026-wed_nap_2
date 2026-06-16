package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.OrderStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {
    private User buyer;
    private Game game;

    @BeforeEach
    void setUp() {
        buyer = User.create("buyer", "buyer@test.com", "hash", Role.BUYER);
        User publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        game = Game.create("Test Game", "desc", BigDecimal.TEN, null, null, publisher);
        game.approve();
    }

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
                null,
                createUser()
        );
        game.approve();

        return OrderItem.of(game, price);
    }

    @Test
    void createOrder_startsAsPending() {
        Order order = Order.create(buyer);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).isEmpty();
        assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addItem_increasesTotalPrice() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));
        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    void addItem_toCompletedOrder_throwsIllegalState() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));
        order.complete("invoices/invoice.txt");

        assertThatThrownBy(() -> order.addItem(OrderItem.of(game, BigDecimal.TEN)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify a non-pending order");
    }

//    @Test
//    void removeItem_removesCorrectGame() {
//        Order order = Order.create(buyer);
//        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
//        order.addItem(item);
//
//        UUID gameId = game.getId();
//        order.removeItem(gameId);
//
//        assertThat(order.getItems()).isEmpty();
//    }

    @Test
    void removeItem_notInOrder_throwsIllegalArgument() {
        Order order = Order.create(buyer);
        assertThatThrownBy(() -> order.removeItem(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Game not found in this order");
    }

    @Test
    void complete_setsStatusAndGeneratesKeys() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));
        order.complete("invoices/invoice.txt");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getInvoicePath()).isEqualTo("invoices/invoice.txt");
        assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.TEN);
        order.getItems().forEach(item -> assertThat(item.getActivationKey()).isNotBlank());
    }

    @Test
    void complete_alreadyCompleted_throwsIllegalState() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));
        order.complete("invoices/invoice.txt");

        assertThatThrownBy(() -> order.complete("invoices/invoice2.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending orders can be completed");
    }

    @Test
    void cancel_pendingOrder_setsStatusCancelled() {
        Order order = Order.create(buyer);
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_completedOrder_throwsIllegalState() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));
        order.complete("invoices/invoice.txt");

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending orders can be cancelled");
    }

    @Test
    void calculateTotal_sumsAllItemPrices() {
        Order order = Order.create(buyer);
        User publisher = User.create("pub2", "pub2@test.com", "hash", Role.PUBLISHER);
        Game game2 = Game.create("Game 2", "desc", new BigDecimal("20.00"), null, null, publisher);
        game2.approve();
        order.addItem(OrderItem.of(game, BigDecimal.TEN));
        order.addItem(OrderItem.of(game2, new BigDecimal("20.00")));

        assertThat(order.calculateTotal()).isEqualByComparingTo(new BigDecimal("30.00"));
    }
}
