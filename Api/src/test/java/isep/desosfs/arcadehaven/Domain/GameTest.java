package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class GameTest {
    @BeforeEach
    void setUp() {}

    @Test
    void shouldCreateGame() {
        User publisher = User.create(
                "publisher",
                "publisher@test.com",
                "hash",
                Role.PUBLISHER
        );

        Game game = Game.create(
                "Game 1",
                "Description",
                BigDecimal.valueOf(19.99),
                "rawg123",
                null,
                publisher
        );

        assertEquals("Game 1", game.getTitle());
        assertEquals("Description", game.getDescription());
        assertEquals(BigDecimal.valueOf(19.99), game.getPrice());
        assertEquals(GameStatus.PENDING, game.getStatus());
        assertEquals(publisher, game.getPublisher());
    }

    @Test
    void shouldApprovePendingGame() {
        Game game = createGame();

        game.approve();

        assertEquals(GameStatus.ACTIVE, game.getStatus());
    }

    @Test
    void shouldThrowWhenApprovingNonPendingGame() {
        Game game = createGame();

        game.approve();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                game::approve
        );

        assertEquals("Only pending games can be approved", ex.getMessage());
    }

    @Test
    void shouldRemoveGame() {
        Game game = createGame();

        game.remove();

        assertEquals(GameStatus.REMOVED, game.getStatus());
    }

    @Test
    void shouldUpdatePrice() {
        Game game = createGame();

        game.updatePrice(BigDecimal.valueOf(49.99));

        assertEquals(BigDecimal.valueOf(49.99), game.getPrice());
    }

    @Test
    void shouldThrowWhenPriceIsInvalid() {
        Game game = createGame();

        assertThrows(
                IllegalArgumentException.class,
                () -> game.updatePrice(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldUpdateDetails() {
        Game game = createGame();

        game.updateDetails("New Title", "New Description", null);

        assertEquals("New Title", game.getTitle());
        assertEquals("New Description", game.getDescription());
    }

    @Test
    void shouldAddFile() {
        Game game = createGame();

        game.addFile(
                "cover.png",
                "/images/cover.png",
                FileType.COVER
        );

        assertEquals(1, game.getFiles().size());

        GameFile file = game.getFiles().get(0);

        assertEquals("cover.png", file.getFilename());
        assertEquals(FileType.COVER, file.getFileType());
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
                "Description",
                BigDecimal.valueOf(10),
                "rawg",
                null,
                publisher
        );
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        User publisher = User.create("publisher", "publisher@test.com", "hash", Role.PUBLISHER);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Game.create("Game", "Description", null, "rawg", null, publisher)
        );

        assertEquals("Price cannot be null", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceHasMoreThanTwoDecimalPlaces() {
        User publisher = User.create("publisher", "publisher@test.com", "hash", Role.PUBLISHER);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Game.create("Game", "Description", new BigDecimal("9.999"), "rawg", null, publisher)
        );

        assertEquals("A maximum of 2 decimal places allowed", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceExceedsMaximum() {
        User publisher = User.create("publisher", "publisher@test.com", "hash", Role.PUBLISHER);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Game.create("Game", "Description", new BigDecimal("1000000.00"), "rawg", null, publisher)
        );

        assertEquals("Price cannot be larger than 999999.99", ex.getMessage());
    }

    @Test
    void shouldAcceptPriceAtMaximumBoundary() {
        User publisher = User.create("publisher", "publisher@test.com", "hash", Role.PUBLISHER);

        Game game = Game.create("Game", "Description", new BigDecimal("999999.99"), "rawg", null, publisher);

        assertEquals(new BigDecimal("999999.99"), game.getPrice());
    }

    @Test
    void shouldThrowWhenUpdatePriceIsNull() {
        Game game = createGame();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> game.updatePrice(null)
        );

        assertEquals("Price must be greater than zero", ex.getMessage());
    }

    @Test
    void shouldNotUpdateTitleWhenNull() {
        Game game = createGame();

        game.updateDetails(null, "New Description", null);

        assertEquals("Game", game.getTitle());
        assertEquals("New Description", game.getDescription());
    }

    @Test
    void shouldNotUpdateTitleWhenBlank() {
        Game game = createGame();

        game.updateDetails("   ", "New Description", null);

        assertEquals("Game", game.getTitle());
    }

    @Test
    void shouldNotUpdateDescriptionWhenNull() {
        Game game = createGame();

        game.updateDetails("New Title", null, null);

        assertEquals("New Title", game.getTitle());
        assertEquals("Description", game.getDescription());
    }

    @Test
    void shouldUpdateCategoryWhenProvided() {
        Game game = createGame();

        game.updateDetails(null, null, "RPG");

        assertEquals("RPG", game.getCategory());
    }

    @Test
    void shouldNotUpdateCategoryWhenBlank() {
        Game game = createGame();
        game.updateDetails(null, null, "RPG");

        game.updateDetails(null, null, "   ");

        assertEquals("RPG", game.getCategory());
    }

    @Test
    void shouldThrowWhenRejectingNonPendingGame() {
        Game game = createGame();
        game.reject();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                game::reject
        );

        assertEquals("Only pending games can be rejected", ex.getMessage());
    }
}
