package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Order;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    public String buildInvoiceContent(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("INVOICE ID: ").append(order.getId()).append("\n");
        sb.append("DATE: ").append(order.getCreatedAt()).append("\n\n");
        order.getItems().forEach(item -> {
            sb.append(sanitizeLine(item.getGame().getTitle()))
                    .append(" - ")
                    .append(item.getPrice())
                    .append("\n");
        });
        sb.append("\nTOTAL: ").append(order.calculateTotal());
        return sb.toString();
    }

    // ASVS V1.1.2 — strip control chars from user-controlled data before embedding in text files
    private static String sanitizeLine(String value) {
        return value.replaceAll("[\\r\\n\\t]", " ").trim();
    }
}
