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
            sb.append(item.getGame().getTitle())
                    .append(" - ")
                    .append(item.getPrice())
                    .append("\n");
        });

        sb.append("\nTOTAL: ").append(order.calculateTotal());

        return sb.toString();
    }
}
