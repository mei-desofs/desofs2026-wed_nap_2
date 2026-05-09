package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Order;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;
    private final InvoiceService invoiceService;

    public FileStorageService(@Value("${storage.path:storage}") String storagePath,
                              InvoiceService invoiceService) {
        this.rootLocation = Paths.get(storagePath);
        this.invoiceService = invoiceService;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation.resolve("games/images"));
            Files.createDirectories(rootLocation.resolve("invoices"));
            Files.createDirectories(rootLocation.resolve("activation-keys"));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directories", e);
        }
    }

    public String saveFile(MultipartFile file, String subdir) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        String cleanName = StringUtils.cleanPath(originalFilename);
        if (cleanName.contains("..")) {
            throw new IllegalArgumentException("Invalid file path sequence in filename");
        }
        String filename = UUID.randomUUID() + "_" + cleanName;
        Path target = rootLocation.resolve(subdir).resolve(filename).normalize();
        if (!target.startsWith(rootLocation.resolve(subdir).normalize())) {
            throw new IllegalArgumentException("File path resolves outside allowed directory");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public void generateInvoice(Order order) throws IOException {
        Path invoicePath = rootLocation
                .resolve("invoices")
                .resolve("invoice_" + order.getId() + ".txt");
        String content = invoiceService.buildInvoiceContent(order);
        Files.writeString(invoicePath, content);
    }
}
