package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User buyer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private BigDecimal totalPrice = BigDecimal.ZERO;

    private String invoicePath;

    private LocalDateTime createdAt = LocalDateTime.now();

    public void complete(String invoicePath) {
        this.status = OrderStatus.COMPLETED;
        this.invoicePath = invoicePath;

        this.items.forEach(OrderItem::generateActivationKey);
        this.totalPrice = calculateTotal();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}