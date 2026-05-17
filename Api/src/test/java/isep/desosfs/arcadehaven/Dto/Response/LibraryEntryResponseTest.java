package isep.desosfs.arcadehaven.Dto.Response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.LibraryEntry;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class LibraryEntryResponseTest {
   @Test
    void whenFromEntry_thenMappingCorrect() {
        User publisher = User.create("pub", "pub@example.com", "hash", Role.BUYER);
        Game game = Game.create("Game", "Desc", BigDecimal.valueOf(10), null, null, publisher);
        LibraryEntry entry = LibraryEntry.of(game, "KEY123");

        LibraryEntryResponse response = LibraryEntryResponse.from(entry);

        assertThat(response.id()).isEqualTo(entry.getId());
        assertThat(response.gameId()).isEqualTo(game.getId());
        assertThat(response.gameTitle()).isEqualTo(game.getTitle());
        assertThat(response.activationKey()).isEqualTo(entry.getActivationKey());
        assertThat(response.status()).isEqualTo(entry.getStatus().name());
        assertThat(response.acquiredAt()).isEqualTo(entry.getAcquiredAt());
    }
}
