package isep.desosfs.arcadehaven.Dto.Response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterResponseTest {
    @Test
    void whenCreatingRegisterResponse_thenFieldsCorrect() {
        RegisterResponse response = new RegisterResponse("user123", "USER");

        assertThat(response.username()).isEqualTo("user123");
        assertThat(response.role()).isEqualTo("USER");
    }
}
