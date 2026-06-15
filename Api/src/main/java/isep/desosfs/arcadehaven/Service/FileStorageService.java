package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final String SFTP_ROOT = "/";
    private static final Set<String> ALLOWED_ROOTS = Set.of("games", "invoices", "keys");

    private final String baseRemoteDir;
    private final InvoiceService invoiceService;
    private final StorageService storageService;

    public FileStorageService(@Value("${sftp.remote-dir}") String baseRemoteDir,
                              StorageService storageService,
                              InvoiceService invoiceService) {
        this.baseRemoteDir = baseRemoteDir;
        this.storageService = storageService;
        this.invoiceService = invoiceService;
    }

    // ASVS V5.3.2
    private void validateRemotePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("File path must not be empty");
        }

        String normalized = path.replace("\\", "/");

        // Traversal attempt
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Path traversal detected");
        }

        // Cannot be absolute path
        if (normalized.startsWith("/")) {
            throw new IllegalArgumentException("Absolute paths not allowed");
        }

        // Validate top level for allowed roots
        String topLevel = normalized.split("/")[0];
        if (!ALLOWED_ROOTS.contains(topLevel)) {
            throw new IllegalArgumentException("Invalid storage area");
        }
    }

    public String saveFile(MultipartFile file, String subdir) throws StorageException {
        validateRemotePath(subdir);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        return storageService.uploadFile(file, subdir);
    }

    // ASVS V5.3.1
    public byte[] downloadFile(String remotePath) throws StorageException {
        validateRemotePath(remotePath);

        return storageService.downloadFile(remotePath);
    }

    public void deleteFile(String remotePath) throws StorageException {
        validateRemotePath(remotePath);

        storageService.deleteFile(remotePath);
    }

    public String generateInvoice(Order order) throws StorageException {
        String content = invoiceService.buildInvoiceContent(order);

        // ASVS 1.3.3
        String filename = "invoice_" + sanitizeFilename(String.valueOf(order.getId())) + "_" + UUID.randomUUID() + ".txt";

        return storageService.uploadBytes(content.getBytes(StandardCharsets.UTF_8), filename, "invoices");
    }

    public String saveActivationKeysFile(Order order) throws StorageException {
        StringBuilder sb = new StringBuilder();
        sb.append("ArcadeHaven — Activation Keys\n");
        sb.append("Order ID: ").append(order.getId()).append("\n\n");
        order.getItems().forEach(item ->
                sb.append(sanitizeText(item.getGame().getTitle()))
                  .append(": ")
                  .append(sanitizeText(item.getActivationKey()))
                  .append("\n"));

        // ASVS 1.3.3
        String filename = "keys_" + sanitizeFilename(String.valueOf(order.getId())) + ".txt";

        return storageService.uploadBytes(sb.toString().getBytes(StandardCharsets.UTF_8), filename, "keys");
    }

    // ASVS 1.3.3
    private String sanitizeFilename(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    // ASVS 1.3.3
    private String sanitizeText(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\r\\n\\t]", " ").trim();
    }
}
