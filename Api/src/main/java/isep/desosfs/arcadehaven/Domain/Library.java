package isep.desosfs.arcadehaven.Domain;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "libraries")
@Getter
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LibraryEntry> entries = new ArrayList<>();

    public void addGame(Game game, String activationKey) {
        LibraryEntry entry = new LibraryEntry();
        entry.getClass(); // placeholder safe init if needed
        entries.add(entry);
    }

    public boolean ownsGame(UUID gameId) {
        return entries.stream()
                .anyMatch(e -> e.getGame().getId().equals(gameId));
    }

    public List<LibraryEntry> getEntries() {
        return entries;
    }
}