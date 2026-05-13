package isep.desosfs.arcadehaven.Config;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFileTypeConverter implements Converter<String, FileType> {
    @Override
    public FileType convert(String source) {
        return FileType.valueOf(source.trim().toUpperCase());
    }
}
