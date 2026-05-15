package isep.desosfs.arcadehaven.Dto.Request;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        RegisterRequest request = new RegisterRequest("user123", "user@example.com", "password123", Role.BUYER);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenInvalidEmail_thenViolation() {
        RegisterRequest request = new RegisterRequest("user123", "invalid-email", "password123", Role.BUYER);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void whenShortPassword_thenViolation() {
        RegisterRequest request = new RegisterRequest("user123", "user@example.com", "short", Role.BUYER);
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
