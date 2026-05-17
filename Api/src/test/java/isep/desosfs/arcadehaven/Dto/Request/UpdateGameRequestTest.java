package isep.desosfs.arcadehaven.Dto.Request;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class UpdateGameRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        UpdateGameRequest request = new UpdateGameRequest("Title", "Desc", BigDecimal.valueOf(10.5), null);
        Set<ConstraintViolation<UpdateGameRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenPriceTooLow_thenViolation() {
        UpdateGameRequest request = new UpdateGameRequest("Title", "Desc", BigDecimal.valueOf(0), null);
        Set<ConstraintViolation<UpdateGameRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }
}
