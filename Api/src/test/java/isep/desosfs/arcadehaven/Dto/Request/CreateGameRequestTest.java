package isep.desosfs.arcadehaven.Dto.Request;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateGameRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        CreateGameRequest request = new CreateGameRequest(
                "Game Title",
                "Description",
                BigDecimal.valueOf(19.99),
                "RAWG123",
                null
        );
        Set<ConstraintViolation<CreateGameRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenBlankTitle_thenViolation() {
        CreateGameRequest request = new CreateGameRequest(
                "",
                "Description",
                BigDecimal.valueOf(19.99),
                "RAWG123",
                null
        );
        Set<ConstraintViolation<CreateGameRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    void whenPriceTooLow_thenViolation() {
        CreateGameRequest request = new CreateGameRequest(
                "Title",
                "Desc",
                BigDecimal.valueOf(0),
                "RAWG123",
                null
        );
        Set<ConstraintViolation<CreateGameRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }
}
