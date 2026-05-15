package isep.desosfs.arcadehaven.Dto.Response;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

public class LibraryResponseTest {
    @Test
    void whenFromLibrary_thenMappingCorrect() {
        User user = User.create("user1", "user1@example.com", "hash", Role.BUYER);
        Library library = Library.create(user);

        Game game = Game.create("Game", "Desc", BigDecimal.valueOf(10), null, user);
        library.addGame(game, "KEY123");

        LibraryResponse response = LibraryResponse.from(library);

        assertThat(response.id()).isEqualTo(library.getId());
        assertThat(response.entries()).hasSize(1);
        assertThat(response.entries().get(0).gameId()).isEqualTo(game.getId());
        assertThat(response.createdAt()).isEqualTo(library.getCreatedAt());
    }
}
