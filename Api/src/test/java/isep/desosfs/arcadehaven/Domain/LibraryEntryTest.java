package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.EntryStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class LibraryEntryTest {

    private static final String VALID_ACTIVATION_KEY = "1A2B3C4D5E6F7890ABCDEF1234567890";

    @Test
    void shouldCreateLibraryEntry() {
        Game game = createGame();
        game.approve();

        LibraryEntry entry = LibraryEntry.of(game, VALID_ACTIVATION_KEY);

        assertEquals(game, entry.getGame());
        assertEquals(VALID_ACTIVATION_KEY, entry.getActivationKey());
        assertEquals(EntryStatus.ACTIVE, entry.getStatus());
    }

    @Test
    void shouldRefundEntry() {
        Game game = createGame();
        game.approve();

        LibraryEntry entry = LibraryEntry.of(game, VALID_ACTIVATION_KEY);

        entry.refund();

        assertEquals(EntryStatus.REFUNDED, entry.getStatus());
    }

    @Test
    void shouldSuspendEntry() {
        LibraryEntry entry = LibraryEntry.of(createGame(), VALID_ACTIVATION_KEY);

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
                null,
                publisher
        );
    }
}