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
        OrderItem item = new OrderItem();
        item.game = game;
        item.price = price;
        return item;
    }

    public void generateActivationKey() {
        // ASVS V11.5.1 — 128-bit CSPRNG entropy; UUID.randomUUID() does not satisfy this requirement
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        this.activationKey = HexFormat.of().formatHex(bytes).toUpperCase();
    }
}
