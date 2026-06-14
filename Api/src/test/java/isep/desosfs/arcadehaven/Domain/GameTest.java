package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GameTest {
    private User publisher;

    @BeforeEach
    void setUp() {
        publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
    }

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

    @Test
    void create_startsAsPending() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        assertThat(game.getStatus()).isEqualTo(GameStatus.PENDING);
        assertThat(game.getTitle()).isEqualTo("Title");
        assertThat(game.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void approve_pendingGame_becomesActive() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.approve();
        assertThat(game.getStatus()).isEqualTo(GameStatus.ACTIVE);
    }

    @Test
    void approve_activeGame_throwsIllegalState() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.approve();

        assertThatThrownBy(game::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending games can be approved");
    }

    @Test
    void reject_pendingGame_becomesRejected() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.reject();
        assertThat(game.getStatus()).isEqualTo(GameStatus.REJECTED);
    }

    @Test
    void reject_activeGame_throwsIllegalState() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.approve();

        assertThatThrownBy(game::reject)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending games can be rejected");
    }

    @Test
    void remove_anyGame_becomesRemoved() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.approve();
        game.remove();
        assertThat(game.getStatus()).isEqualTo(GameStatus.REMOVED);
    }

    @Test
    void updatePrice_validPrice_updatesPrice() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.updatePrice(new BigDecimal("25.99"));
        assertThat(game.getPrice()).isEqualByComparingTo(new BigDecimal("25.99"));
    }

    @Test
    void updatePrice_zero_throwsIllegalArgument() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        assertThatThrownBy(() -> game.updatePrice(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be greater than zero");
    }

    @Test
    void updatePrice_negative_throwsIllegalArgument() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        assertThatThrownBy(() -> game.updatePrice(new BigDecimal("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateDetails_updatesOnlyNonNullFields() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.updateDetails("New Title", null, null);
        assertThat(game.getTitle()).isEqualTo("New Title");
        assertThat(game.getDescription()).isEqualTo("desc");
    }

    @Test
    void updateDetails_blankTitle_doesNotChangeTitle() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, null, publisher);
        game.updateDetails("  ", "new desc", null);
        assertThat(game.getTitle()).isEqualTo("Title");
        assertThat(game.getDescription()).isEqualTo("new desc");
    }
}
