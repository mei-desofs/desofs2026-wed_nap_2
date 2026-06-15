package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.OrderStatus;
import isep.desosfs.arcadehaven.Exception.BusinessException;
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
    private static final BigDecimal MAX_TOTAL = new BigDecimal("1000000.00");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    private String invoicePath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Order() {}

    public static Order create(User buyer) {
        Order order = new Order();
        order.buyer = buyer;
        return order;
    }

    public void addItem(OrderItem item) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify a non-pending order");
        }
        this.items.add(item);
    }

    public void removeItem(UUID gameId) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify a non-pending order");
        }
        boolean removed = this.items.removeIf(i -> i.getGame().getId().equals(gameId));
        if (!removed) {
            throw new IllegalArgumentException("Game not found in this order");
        }
    }

    public void complete(String invoicePath) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be completed");
        }
        this.items.forEach(OrderItem::generateActivationKey);
        this.totalPrice = calculateTotal();
        this.invoicePath = invoicePath;
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(MAX_TOTAL) > 0) {
            throw new BusinessException("Order total exceeds allowed maximum");
        }

        return total;
    }
}
