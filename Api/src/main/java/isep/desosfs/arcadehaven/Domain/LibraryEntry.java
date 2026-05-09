package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.EntryStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "library_entries")
@Getter
public class LibraryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String activationKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime acquiredAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status = EntryStatus.ACTIVE;

    protected LibraryEntry() {}

    public static LibraryEntry of(Game game, String activationKey) {
        LibraryEntry entry = new LibraryEntry();
        entry.game = game;
        entry.activationKey = activationKey;
        return entry;
    }

    public void refund() {
        this.status = EntryStatus.REFUNDED;
    }

    public void suspend() {
        this.status = EntryStatus.SUSPENDED;
    }
}
