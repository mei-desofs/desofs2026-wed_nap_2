package isep.desosfs.arcadehaven.Domain;


import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Game game;

    private BigDecimal price;

    private String activationKey;

    public void generateActivationKey() {
        this.activationKey = UUID.randomUUID().toString();
    }
}
