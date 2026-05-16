package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Exception.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileStorageService {

    private final InvoiceService invoiceService;
    private final SftpStorageService sftpStorageService;

    public FileStorageService(SftpStorageService sftpStorageService,
                              InvoiceService invoiceService) {
        this.sftpStorageService = sftpStorageService;
        this.invoiceService = invoiceService;
    }

    public String saveFile(MultipartFile file, String subdir) throws StorageException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return sftpStorageService.uploadFile(file, subdir);
    }

    public byte[] downloadFile(String remotePath) throws StorageException {
        validateRemotePath(remotePath);
        return sftpStorageService.downloadFile(remotePath);
    }

    private void validateRemotePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("File path must not be empty");
        }
        // Prevent path traversal: reject any sequence that navigates up directories
        String normalized = path.replace("\\", "/");
        if (normalized.contains("/../") || normalized.endsWith("/..") || normalized.startsWith("../") || normalized.equals("..")) {
            throw new IllegalArgumentException("Invalid file path: directory traversal is not allowed");
        }
    }

    public void deleteFile(String remotePath) throws StorageException {
        sftpStorageService.deleteFile(remotePath);
    }

    public String generateInvoice(Order order) throws StorageException {
        String content = invoiceService.buildInvoiceContent(order);
        String filename = "invoice_" + order.getId() + "_" + UUID.randomUUID() + ".txt";
        return sftpStorageService.uploadBytes(content.getBytes(StandardCharsets.UTF_8), filename, "invoices");
    }

    public String saveActivationKeysFile(Order order) throws StorageException {
        StringBuilder sb = new StringBuilder();
        sb.append("ArcadeHaven — Activation Keys\n");
        sb.append("Order ID: ").append(order.getId()).append("\n\n");
        order.getItems().forEach(item ->
                sb.append(item.getGame().getTitle())
                  .append(": ")
                  .append(item.getActivationKey())
                  .append("\n"));
        String filename = "keys_" + order.getId() + ".txt";
        return sftpStorageService.uploadBytes(sb.toString().getBytes(StandardCharsets.UTF_8), filename, "keys");
    }
}
