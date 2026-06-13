package isep.desosfs.arcadehaven.Domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
public class OrderItem {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String activationKey;

    protected OrderItem() {}

    public static OrderItem of(Game game, BigDecimal price) {
        validatePrice(price);

        OrderItem item = new OrderItem();
        item.game = game;
        item.price = price;
        return item;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }

        if (price.scale() > 2) {
            throw new IllegalArgumentException("A maximum of 2 decimal places allowed");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be above 0");
        }

        if (price.compareTo(new BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Price cannot be larger than 999999.99");
        }
    }

    public void generateActivationKey() {
        // ASVS V11.5.1 — 128-bit CSPRNG entropy; UUID.randomUUID() does not satisfy this requirement
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        this.activationKey = HexFormat.of().formatHex(bytes).toUpperCase();
    }
}
