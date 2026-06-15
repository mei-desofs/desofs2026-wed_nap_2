package isep.desosfs.arcadehaven.Validation;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

// ASVS V5.1.1
@Component
public class FileValidator {
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpg",
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    private long maxFileSizeBytes;

    private static final Tika TIKA = new Tika();

    @PostConstruct
    void init() {
        maxFileSizeBytes = DataSize
                .parse(maxFileSize)
                .toBytes();
    }

    // ASVS V5.2.1
    public  void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSizeBytes) {
            throw new MaxUploadSizeExceededException(maxFileSizeBytes);
        }
    }

    // ASVS V5.2.2
    public void validateMimeType(MultipartFile file, FileType fileType) {
        String detectedType;

        try {
            detectedType = TIKA.detect(file.getInputStream());
        } catch (IOException e) {
            throw new StorageException("Cannot read uploaded file for type detection", e);
        }

        if (detectedType == null) {
            throw new IllegalArgumentException("Cannot detect file type");
        }

        switch (fileType) {
            case IMAGE, SCREENSHOT, COVER -> {
                if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
                    throw new IllegalArgumentException(
                            "Invalid file type: " + detectedType);
                }
            }
        }
    }

    // ASVS V5.2.2
    public void validateExtension(MultipartFile file, FileType fileType) {
        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Missing file name");
        }

        // normalize Windows paths
        filename = filename.replace("\\", "/");
        filename = filename.substring(filename.lastIndexOf("/") + 1);

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            throw new IllegalArgumentException("Missing file extension");
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase();

        switch (fileType) {
            case IMAGE, SCREENSHOT, COVER -> {
                if (!List.of("jpg", "jpeg", "png", "webp").contains(extension)) {
                    throw new IllegalArgumentException("Unsupported image extension: " + extension);
                }
            }
        }
    }
}