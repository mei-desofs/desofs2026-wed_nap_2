package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Local-filesystem storage backend — active when storage.backend=local.
 * Intended for development and CI environments without SFTP access.
 *
 * RF-31 / RF-32: required sub-directories (invoices, keys, games/images) are
 * created at startup via {@link #ensureBaseDirectories()}.
 */
@Service
@ConditionalOnProperty(name = "storage.backend", havingValue = "local")
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path baseDir;

    public LocalStorageService(@Value("${storage.path:storage}") String basePath) {
        this.baseDir = Path.of(basePath).toAbsolutePath().normalize();
    }

    // RF-31 / RF-32 — pre-create required directories at startup
    @EventListener(ApplicationReadyEvent.class)
    public void ensureBaseDirectories() {
        try {
            for (String sub : List.of("invoices", "keys", "games/images")) {
                Files.createDirectories(baseDir.resolve(sub));
            }
            log.info("Local storage initialized at {}", baseDir);
        } catch (IOException e) {
            log.error("Failed to initialize local storage directories at {}: {}", baseDir, e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String subDir) throws StorageException {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        try {
            Path target = resolveAndValidate(subDir, UUID.randomUUID() + "_" + original);
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target);
            return baseDir.relativize(target).toString().replace("\\", "/");
        } catch (IOException e) {
            throw new StorageException("Failed to save file locally: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadBytes(byte[] data, String filename, String subDir) throws StorageException {
        try {
            Path target = resolveAndValidate(subDir, filename);
            Files.createDirectories(target.getParent());
            Files.write(target, data);
            return baseDir.relativize(target).toString().replace("\\", "/");
        } catch (IOException e) {
            throw new StorageException("Failed to write file locally: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String path) throws StorageException {
        try {
            Path target = baseDir.resolve(path).normalize();
            if (!target.startsWith(baseDir)) {
                throw new StorageException("Path is outside local storage root: " + path, null);
            }
            return Files.readAllBytes(target);
        } catch (StorageException e) {
            throw e;
        } catch (IOException e) {
            throw new StorageException("Failed to read file locally: " + path, e);
        }
    }

    @Override
    public void deleteFile(String path) throws StorageException {
        try {
            Path target = baseDir.resolve(path).normalize();
            if (!target.startsWith(baseDir)) {
                throw new StorageException("Path is outside local storage root: " + path, null);
            }
            Files.deleteIfExists(target);
        } catch (StorageException e) {
            throw e;
        } catch (IOException e) {
            throw new StorageException("Failed to delete file locally: " + path, e);
        }
    }

    private Path resolveAndValidate(String subDir, String filename) {
        Path target = baseDir.resolve(subDir).resolve(filename).normalize();
        if (!target.startsWith(baseDir)) {
            throw new StorageException("Resolved path escapes storage root", null);
        }
        return target;
    }
}
