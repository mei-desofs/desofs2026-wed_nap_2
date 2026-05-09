package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.PENDING;

    private String rawgApiId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;

    @ElementCollection
    @CollectionTable(name = "game_files", joinColumns = @JoinColumn(name = "game_id"))
    private List<GameFile> files = new ArrayList<>();

    protected Game() {}

    public static Game create(String title, String description, BigDecimal price, String rawgApiId, User publisher) {
        Game game = new Game();
        game.title = title;
        game.description = description;
        game.price = price;
        game.rawgApiId = rawgApiId;
        game.publisher = publisher;
        return game;
    }

    public void approve() {
        if (status != GameStatus.PENDING) {
            throw new IllegalStateException("Only pending games can be approved");
        }
        this.status = GameStatus.ACTIVE;
    }

    public void remove() {
        this.status = GameStatus.REMOVED;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.price = newPrice;
    }

    public void updateDetails(String title, String description) {
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null) this.description = description;
    }

    public void addFile(String filename, String path, FileType type) {
        this.files.add(new GameFile(filename, path, type, LocalDateTime.now()));
    }
}
