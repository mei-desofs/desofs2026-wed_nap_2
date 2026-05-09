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
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "library_id")
    private List<LibraryEntry> entries = new ArrayList<>();

    protected Library() {}

    public static Library create(User user) {
        Library library = new Library();
        library.user = user;
        return library;
    }

    public void addGame(Game game, String activationKey) {
        this.entries.add(LibraryEntry.of(game, activationKey));
    }

    public boolean ownsGame(UUID gameId) {
        return entries.stream()
                .anyMatch(e -> e.getGame().getId().equals(gameId));
    }

    public List<LibraryEntry> getEntries() {
        return entries;
    }
}
