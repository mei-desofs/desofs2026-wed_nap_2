package isep.desosfs.arcadehaven.Validation;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileValidator {
    private final Tika tika = new Tika();

    public void validateMimeType(MultipartFile file, FileType fileType) throws IOException {
        String detectedMimeType = tika.detect(file.getInputStream());

        if (detectedMimeType == null || detectedMimeType.isBlank()) {
            throw new IllegalArgumentException("Could not detect MIME type");
        }

        switch (fileType) {

            case IMAGE, SCREENSHOT, COVER -> {
                if (!detectedMimeType.startsWith("image/")) {
                    throw new IllegalArgumentException(
                            "Invalid image MIME type: " + detectedMimeType
                    );
                }
            }
        }
    }
}