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

        when(order.getId()).thenReturn(UUID.randomUUID());
        when(order.calculateTotal()).thenReturn(BigDecimal.TEN);

        String result = invoiceService.buildInvoiceContent(order);

        assertTrue(result.contains("INVOICE ID"));
    }
}
