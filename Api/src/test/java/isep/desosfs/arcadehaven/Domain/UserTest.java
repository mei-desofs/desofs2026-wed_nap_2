package isep.desosfs.arcadehaven.Domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.Role;

public class UserTest {
    @Test
    void shouldCreateUser() {
        User user = User.create(
                "user",
                "user@test.com",
                "hash",
                Role.BUYER
        );

        assertEquals("user", user.getUsername());
        assertEquals("user@test.com", user.getEmail());
        assertEquals(Role.BUYER, user.getRole());
        assertTrue(user.isActive());
    }

    @Test
    void shouldDeactivateUser() {
        User user = createUser();

        user.deactivate();

        assertFalse(user.isActive());
    }

    @Test
    void shouldActivateUser() {
        User user = createUser();

        user.deactivate();
        user.activate();

        assertTrue(user.isActive());
    }

    @Test
    void shouldChangeRole() {
        User user = createUser();

        user.changeRole(Role.PUBLISHER);

        assertEquals(Role.PUBLISHER, user.getRole());
    }

    @Test
    void shouldThrowWhenChangingAdminRole() {
        User admin = User.create(
                "admin",
                "admin@test.com",
                "hash",
                Role.ADMIN
        );

        assertThrows(
                IllegalStateException.class,
                () -> admin.changeRole(Role.BUYER)
        );
    }

    private User createUser() {
        return User.create(
                "user",
                "user@test.com",
                "hash",
                Role.BUYER
        );
    }
}
