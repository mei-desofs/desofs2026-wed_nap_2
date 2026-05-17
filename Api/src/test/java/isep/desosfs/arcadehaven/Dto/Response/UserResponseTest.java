package isep.desosfs.arcadehaven.Dto.Response;

import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.Role;

import static org.assertj.core.api.Assertions.assertThat;

public class UserResponseTest {
    @Test
    void whenFromUser_thenMappingCorrect() {
        User user = User.create("user1", "user1@example.com", "hash", Role.BUYER);

        UserResponse response = UserResponse.from(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.role()).isEqualTo(user.getRole().name());
        assertThat(response.active()).isEqualTo(user.isActive());
        assertThat(response.createdAt()).isEqualTo(user.getCreatedAt());
    }
}
