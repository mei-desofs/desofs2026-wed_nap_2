package isep.desosfs.arcadehaven.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return !HTML_PATTERN.matcher(value).find();
    }
}
