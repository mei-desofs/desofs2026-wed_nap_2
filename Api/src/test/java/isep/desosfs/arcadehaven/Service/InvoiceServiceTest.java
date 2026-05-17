package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Order;

public class InvoiceServiceTest {
    private final InvoiceService invoiceService = new InvoiceService();

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
}
