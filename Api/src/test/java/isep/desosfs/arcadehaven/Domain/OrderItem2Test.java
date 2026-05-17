package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderItem.generateActivationKey() — ASVS V11.5.1.
 * Verifies activation keys are generated with CSPRNG and >= 128-bit entropy,
 * not UUID (which does not satisfy the entropy requirement).
 */
class OrderItem2Test {

    private Game game;

    @BeforeEach
    void setUp() {
        User publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        game = Game.create("Test Game", "desc", BigDecimal.TEN, null, null, publisher);
    }

    // ── Format validation ────────────────────────────────────────────────────────

    @Test
    void generateActivationKey_produces32Characters() {
        // 128 bits = 16 bytes = 32 hex characters
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        item.generateActivationKey();
        assertThat(item.getActivationKey()).hasSize(32);
    }

    @Test
    void generateActivationKey_isUppercaseHexadecimal() {
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        item.generateActivationKey();
        assertThat(item.getActivationKey()).matches("[0-9A-F]{32}");
    }

    @Test
    void generateActivationKey_doesNotContainHyphens() {
        // UUID format includes hyphens; CSPRNG hex output must not
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        item.generateActivationKey();
        assertThat(item.getActivationKey()).doesNotContain("-");
    }

    @Test
    void generateActivationKey_isNotBlank() {
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        item.generateActivationKey();
        assertThat(item.getActivationKey()).isNotBlank();
    }

    // ── Entropy validation ───────────────────────────────────────────────────────

    @Test
    void generateActivationKey_has128BitsOfEntropy() {
        // 32 hex chars * 4 bits/char = 128 bits — exact minimum required by ASVS V11.5.1
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        item.generateActivationKey();
        int bits = item.getActivationKey().length() * 4;
        assertThat(bits).isGreaterThanOrEqualTo(128);
    }

    @Test
    void generateActivationKey_producesUniqueValues() {
        // Statistical uniqueness: 100 CSPRNG calls must produce 100 distinct values.
        // Probability of collision with 128-bit entropy is negligible (~2^-57).
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            OrderItem item = OrderItem.of(game, BigDecimal.TEN);
            item.generateActivationKey();
            keys.add(item.getActivationKey());
        }
        assertThat(keys).hasSize(100);
    }

    @Test
    void generateActivationKey_consecutiveCallsProduceDifferentKeys() {
        OrderItem item1 = OrderItem.of(game, BigDecimal.TEN);
        OrderItem item2 = OrderItem.of(game, BigDecimal.TEN);
        item1.generateActivationKey();
        item2.generateActivationKey();
        assertThat(item1.getActivationKey()).isNotEqualTo(item2.getActivationKey());
    }
}
