package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class LibraryTest {
    @Test
    void shouldCreateLibrary() {
        User user = User.create(
                "user",
                "user@test.com",
                "hash",
                Role.BUYER
        );

        Library library = Library.create(user);

        assertEquals(user, library.getUser());
        assertTrue(library.getEntries().isEmpty());
    }

    @Test
    void shouldAddGame() {
        Library library = Library.create(createUser());
        Game game = createGame();

        library.addGame(game, "ABC123");

        assertEquals(1, library.getEntries().size());
    }

    /*@Test
    void shouldOwnGame() {
        Library library = Library.create(createUser());
        Game game = createGame();

        library.addGame(game, "KEY123");

        UUID gameId = game.getId();

        if (gameId == null) {
            assertFalse(library.ownsGame(UUID.randomUUID()));
        }
    }*/

    private User createUser() {
        return User.create(
                "user",
                "user@test.com",
                "hash",
                Role.BUYER
        );
    }

    private Game createGame() {
        return Game.create(
                "Game",
                "Desc",
                BigDecimal.TEN,
                "rawg",
                createUser()
        );
    }
}
