package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameDomainTest {

    private User publisher;

    @BeforeEach
    void setUp() {
        publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
    }

    @Test
    void create_startsAsPending() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        assertThat(game.getStatus()).isEqualTo(GameStatus.PENDING);
        assertThat(game.getTitle()).isEqualTo("Title");
        assertThat(game.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void approve_pendingGame_becomesActive() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.approve();
        assertThat(game.getStatus()).isEqualTo(GameStatus.ACTIVE);
    }

    @Test
    void approve_activeGame_throwsIllegalState() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.approve();

        assertThatThrownBy(game::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending games can be approved");
    }

    @Test
    void reject_pendingGame_becomesRejected() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.reject();
        assertThat(game.getStatus()).isEqualTo(GameStatus.REJECTED);
    }

    @Test
    void reject_activeGame_throwsIllegalState() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.approve();

        assertThatThrownBy(game::reject)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending games can be rejected");
    }

    @Test
    void remove_anyGame_becomesRemoved() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.approve();
        game.remove();
        assertThat(game.getStatus()).isEqualTo(GameStatus.REMOVED);
    }

    @Test
    void updatePrice_validPrice_updatesPrice() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.updatePrice(new BigDecimal("25.99"));
        assertThat(game.getPrice()).isEqualByComparingTo(new BigDecimal("25.99"));
    }

    @Test
    void updatePrice_zero_throwsIllegalArgument() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        assertThatThrownBy(() -> game.updatePrice(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be greater than zero");
    }

    @Test
    void updatePrice_negative_throwsIllegalArgument() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        assertThatThrownBy(() -> game.updatePrice(new BigDecimal("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateDetails_updatesOnlyNonNullFields() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.updateDetails("New Title", null);
        assertThat(game.getTitle()).isEqualTo("New Title");
        assertThat(game.getDescription()).isEqualTo("desc");
    }

    @Test
    void updateDetails_blankTitle_doesNotChangeTitle() {
        Game game = Game.create("Title", "desc", BigDecimal.TEN, null, publisher);
        game.updateDetails("  ", "new desc");
        assertThat(game.getTitle()).isEqualTo("Title");
        assertThat(game.getDescription()).isEqualTo("new desc");
    }
}
