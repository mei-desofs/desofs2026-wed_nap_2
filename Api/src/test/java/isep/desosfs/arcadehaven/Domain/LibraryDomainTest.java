package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryDomainTest {

    private User owner;
    private Game game;

    @BeforeEach
    void setUp() {
        owner = User.create("buyer", "buyer@test.com", "hash", Role.BUYER);
        User publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        game = Game.create("Game", "desc", BigDecimal.TEN, null, null, publisher);
    }

    @Test
    void create_startsEmpty() {
        Library library = Library.create(owner);
        assertThat(library.getEntries()).isEmpty();
        assertThat(library.getUser()).isEqualTo(owner);
    }

    @Test
    void addGame_addsEntry() {
        Library library = Library.create(owner);
        library.addGame(game, "KEY-001");
        assertThat(library.getEntries()).hasSize(1);
        assertThat(library.getEntries().get(0).getActivationKey()).isEqualTo("KEY-001");
    }

//    @Test
//    void ownsGame_afterAdding_returnsTrue() {
//        Library library = Library.create(owner);
//        library.addGame(game, "KEY-001");
//        assertThat(library.ownsGame(game.getId())).isTrue();
//    }

    @Test
    void ownsGame_notAdded_returnsFalse() {
        Library library = Library.create(owner);
        assertThat(library.ownsGame(UUID.randomUUID())).isFalse();
    }

//    @Test
//    void addGame_multipleGames_allTracked() {
//        User publisher = User.create("pub2", "pub2@test.com", "hash", Role.PUBLISHER);
//        Game game2 = Game.create("Game 2", "desc", new BigDecimal("20"), null, null, publisher);
//        Library library = Library.create(owner);
//        library.addGame(game, "KEY-001");
//        library.addGame(game2, "KEY-002");
//
//        assertThat(library.getEntries()).hasSize(2);
//        assertThat(library.ownsGame(game.getId())).isTrue();
//        assertThat(library.ownsGame(game2.getId())).isTrue();
//    }
}
