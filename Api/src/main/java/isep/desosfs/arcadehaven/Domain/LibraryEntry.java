package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.EntryStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private Game game;

    private String activationKey;

    private LocalDateTime acquiredAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EntryStatus status = EntryStatus.ACTIVE;

    public void refund() {
        this.status = EntryStatus.REFUNDED;
    }

    public void suspend() {
        this.status = EntryStatus.SUSPENDED;
    }
}