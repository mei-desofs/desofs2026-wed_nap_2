package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.EntryStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class LibraryEntryTest {
    @Test
    void shouldCreateLibraryEntry() {
        Game game = createGame();

        LibraryEntry entry = LibraryEntry.of(game, "KEY123");

        assertEquals(game, entry.getGame());
        assertEquals("KEY123", entry.getActivationKey());
        assertEquals(EntryStatus.ACTIVE, entry.getStatus());
    }

    @Test
    void shouldRefundEntry() {
        LibraryEntry entry = LibraryEntry.of(createGame(), "KEY");

        entry.refund();

        assertEquals(EntryStatus.REFUNDED, entry.getStatus());
    }

    @Test
    void shouldSuspendEntry() {
        LibraryEntry entry = LibraryEntry.of(createGame(), "KEY");

        entry.suspend();

        assertEquals(EntryStatus.SUSPENDED, entry.getStatus());
    }

    private Game createGame() {
        User publisher = User.create(
                "publisher",
                "publisher@test.com",
                "hash",
                Role.PUBLISHER
        );

        return Game.create(
                "Game",
                "Desc",
                BigDecimal.TEN,
                "rawg",
                publisher
        );
    }
}
