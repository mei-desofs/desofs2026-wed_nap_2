package isep.desosfs.arcadehaven.Domain;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Edge-case and security-relevant tests for User domain entity.
 * Complements UserTest with boundary conditions, idempotence, and role transition coverage.
 */
class UserDomainTest {

    // ── Creation invariants ──────────────────────────────────────────────────

    @Test
    void create_startsActive() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void create_storesUsernameExactly() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        assertThat(user.getUsername()).isEqualTo("alice");
    }

    @Test
    void create_storesEmailExactly() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        assertThat(user.getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    void create_storesPasswordHashVerbatim() {
        // Password hash must be stored as-is; service layer owns hashing
        User user = User.create("alice", "alice@test.com", "bcrypt$hash", Role.BUYER);
        assertThat(user.getPasswordHash()).isEqualTo("bcrypt$hash");
    }

    @Test
    void create_storesRole() {
        User publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        assertThat(publisher.getRole()).isEqualTo(Role.PUBLISHER);
    }

    // ── Deactivation ─────────────────────────────────────────────────────────

    @Test
    void deactivate_setsActiveToFalse() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        user.deactivate();
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void deactivate_alreadyInactive_isIdempotent() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        user.deactivate();
        user.deactivate(); // second call must not throw
        assertThat(user.isActive()).isFalse();
    }

    // ── Activation ───────────────────────────────────────────────────────────

    @Test
    void activate_afterDeactivate_restoresActive() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        user.deactivate();
        user.activate();
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void activate_alreadyActive_isIdempotent() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        user.activate(); // already active — must not throw
        assertThat(user.isActive()).isTrue();
    }

    // ── Role transitions ─────────────────────────────────────────────────────

    @Test
    void changeRole_buyerToPublisher_succeeds() {
        User user = User.create("alice", "alice@test.com", "hash", Role.BUYER);
        user.changeRole(Role.PUBLISHER);
        assertThat(user.getRole()).isEqualTo(Role.PUBLISHER);
    }

    @Test
    void changeRole_publisherToBuyer_succeeds() {
        User user = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        user.changeRole(Role.BUYER);
        assertThat(user.getRole()).isEqualTo(Role.BUYER);
    }

    @Test
    void changeRole_adminToAnyRole_throwsIllegalState() {
        User admin = User.create("admin", "admin@test.com", "hash", Role.ADMIN);
        assertThatThrownBy(() -> admin.changeRole(Role.BUYER))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void changeRole_adminToPublisher_throwsIllegalState() {
        User admin = User.create("admin", "admin@test.com", "hash", Role.ADMIN);
        assertThatThrownBy(() -> admin.changeRole(Role.PUBLISHER))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── Email update ─────────────────────────────────────────────────────────

    @Test
    void updateEmail_changesEmail() {
        User user = User.create("alice", "old@test.com", "hash", Role.BUYER);
        user.updateEmail("new@test.com");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateEmail_consecutiveCalls_keepsLastValue() {
        User user = User.create("alice", "old@test.com", "hash", Role.BUYER);
        user.updateEmail("first@test.com");
        user.updateEmail("second@test.com");
        assertThat(user.getEmail()).isEqualTo("second@test.com");
    }
}
