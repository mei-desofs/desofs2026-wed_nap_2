package isep.desosfs.arcadehaven.Config;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleConverter implements Converter<String, Role> {
    @Override
    public Role convert(String source) {
        return Role.valueOf(source.trim().toUpperCase());
    }
}
