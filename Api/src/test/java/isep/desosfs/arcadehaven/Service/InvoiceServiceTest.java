package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Domain.OrderItem;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InvoiceService.
 * Covers correct invoice formatting and ASVS V1.1.2 output-encoding
 * (control-character sanitization in user-controlled data).
 */
public class InvoiceServiceTest {

    private InvoiceService invoiceService;
    private User buyer;
    private User publisher;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService();
        buyer = User.create("buyer", "buyer@test.com", "hash", Role.BUYER);
        publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
    }

    @Test
    void shouldBuildInvoiceContent() {
        Order order = mock(Order.class);

        UUID id = UUID.randomUUID();

        when(order.getId()).thenReturn(id);
        when(order.calculateTotal()).thenReturn(BigDecimal.TEN);
        when(order.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());

        var game = mock(isep.desosfs.arcadehaven.Domain.Game.class);
        var item = mock(isep.desosfs.arcadehaven.Domain.OrderItem.class);

        when(game.getTitle()).thenReturn("Test Game\nName");
        when(item.getGame()).thenReturn(game);
        when(item.getPrice()).thenReturn(BigDecimal.TEN);

        when(order.getItems()).thenReturn(java.util.List.of(item));

        String result = new InvoiceService().buildInvoiceContent(order);

        assertTrue(result.contains("INVOICE ID"));
        assertTrue(result.contains("DATE"));
        assertTrue(result.contains("TOTAL"));
        assertTrue(result.contains("Test Game Name")); // valida sanitizeLine
    }

    @Test
    void shouldSanitizeControlCharacters() {
        Order order = mock(Order.class);

        when(order.getId()).thenReturn(UUID.randomUUID());
        when(order.calculateTotal()).thenReturn(BigDecimal.ONE);
        when(order.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());

        var game = mock(isep.desosfs.arcadehaven.Domain.Game.class);
        var item = mock(isep.desosfs.arcadehaven.Domain.OrderItem.class);

        when(game.getTitle()).thenReturn("A\rB\tC\nD");
        when(item.getGame()).thenReturn(game);
        when(item.getPrice()).thenReturn(BigDecimal.ONE);

        when(order.getItems()).thenReturn(java.util.List.of(item));

        String result = new InvoiceService().buildInvoiceContent(order);

        assertTrue(result.contains("A B C D"));
    }

    // ── Correct content ──────────────────────────────────────────────────────────

    @Test
    void buildInvoiceContent_containsOrderIdAndDate() {
        Game game = Game.create("Space Rangers", "desc", new BigDecimal("19.99"), null, null, publisher);
        Order order = buildCompletedOrder(game, new BigDecimal("19.99"));

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).contains("INVOICE ID: " + order.getId());
        assertThat(content).contains("DATE:");
    }

    @Test
    void buildInvoiceContent_containsItemTitleAndPrice() {
        Game game = Game.create("Space Rangers", "desc", new BigDecimal("19.99"), null, null, publisher);
        Order order = buildCompletedOrder(game, new BigDecimal("19.99"));

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).contains("Space Rangers");
        assertThat(content).contains("19.99");
    }

    @Test
    void buildInvoiceContent_containsTotalLine() {
        Game game = Game.create("Space Rangers", "desc", new BigDecimal("19.99"), null, null, publisher);
        Order order = buildCompletedOrder(game, new BigDecimal("19.99"));

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).contains("TOTAL:");
    }

    @Test
    void buildInvoiceContent_multipleItems_allListed() {
        Game g1 = Game.create("Alpha Game", "d", new BigDecimal("9.99"), null, null, publisher);
        Game g2 = Game.create("Beta Game", "d", new BigDecimal("14.99"), null, null, publisher);
        g1.approve();
        g2.approve();

        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(g1, new BigDecimal("9.99")));
        order.addItem(OrderItem.of(g2, new BigDecimal("14.99")));
        order.complete("invoices/test.txt");

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).contains("Alpha Game");
        assertThat(content).contains("Beta Game");
        assertThat(content).contains("9.99");
        assertThat(content).contains("14.99");
    }

    // ── ASVS V1.1.2 — control-character sanitization ────────────────────────────

    @Test
    void buildInvoiceContent_newlineInTitle_isReplacedWithSpace() {
        // A publisher could name their game "Real\nFake Key: INJECT" to inject
        // additional lines into the invoice file. The sanitizer must strip this.
        Game game = Game.create("Real\nFake Key: INJECT", "desc", BigDecimal.TEN, null, null, publisher);
        Order order = buildCompletedOrder(game, BigDecimal.TEN);

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).doesNotContain("Real\nFake");
        assertThat(content).contains("Real Fake Key: INJECT");
    }

    @Test
    void buildInvoiceContent_carriageReturnInTitle_isReplacedWithSpace() {
        Game game = Game.create("Evil\rGame", "desc", BigDecimal.TEN, null, null, publisher);
        Order order = buildCompletedOrder(game, BigDecimal.TEN);

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).doesNotContain("Evil\rGame");
        assertThat(content).contains("Evil Game");
    }

    @Test
    void buildInvoiceContent_tabInTitle_isReplacedWithSpace() {
        Game game = Game.create("Evil\tGame", "desc", BigDecimal.TEN, null, null, publisher);
        Order order = buildCompletedOrder(game, BigDecimal.TEN);

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).doesNotContain("Evil\tGame");
        assertThat(content).contains("Evil Game");
    }

    @Test
    void buildInvoiceContent_multipleControlCharsInTitle_allReplaced() {
        Game game = Game.create("A\r\nB\tC", "desc", BigDecimal.TEN, null, null, publisher);
        Order order = buildCompletedOrder(game, BigDecimal.TEN);

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).doesNotContain("\r").doesNotContain("\t");
        assertThat(content).contains("A  B C");
    }

    @Test
    void buildInvoiceContent_cleanTitle_notModified() {
        Game game = Game.create("Space Rangers: Remastered", "desc", BigDecimal.TEN, null, null, publisher);
        Order order = buildCompletedOrder(game, BigDecimal.TEN);

        String content = invoiceService.buildInvoiceContent(order);

        assertThat(content).contains("Space Rangers: Remastered");
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private Order buildCompletedOrder(Game game, BigDecimal price) {
        game.approve();
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, price));
        order.complete("invoices/test.txt");
        return order;
    }
}