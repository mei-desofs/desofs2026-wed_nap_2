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

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.PENDING;

    private String rawgApiId;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    private User publisher;

    @ElementCollection
    @CollectionTable(name = "game_files", joinColumns = @JoinColumn(name = "game_id"))
    private List<GameFile> files = new ArrayList<>();

    public void approve() {
        if (status != GameStatus.PENDING) {
            throw new RuntimeException("Only pending games can be approved");
        }
        this.status = GameStatus.ACTIVE;
    }

    public void remove() {
        this.status = GameStatus.REMOVED;
    }

    public void updatePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid price");
        }
        this.price = price;
    }

    public void addFile(String filename, String path, FileType type) {
        this.files.add(new GameFile(filename, path, type, LocalDateTime.now()));
    }
}