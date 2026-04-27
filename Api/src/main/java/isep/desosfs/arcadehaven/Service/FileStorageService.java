package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Order;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// infrastructure/storage/FileStorageService.java
@Service
public class FileStorageService {

    private final Path rootLocation;

    private final InvoiceService invoiceService;

    public FileStorageService(InvoiceService invoiceService) {
        this.rootLocation = Paths.get("storage");
        this.invoiceService = invoiceService;
    }

    public void init() throws IOException {
        // Cria estrutura de diretórios
        Files.createDirectories(rootLocation.resolve("games/images"));
        Files.createDirectories(rootLocation.resolve("invoices"));
        Files.createDirectories(rootLocation.resolve("activation-keys"));
    }

    public String saveFile(MultipartFile file, String subdir) throws IOException {
        String filename = UUID.randomUUID() + "_" +
                StringUtils.cleanPath(file.getOriginalFilename());
        Path target = rootLocation.resolve(subdir).resolve(filename);
        Files.copy(file.getInputStream(), target,
                StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public void generateInvoice(Order order) throws IOException {
        // Gera ficheiro de fatura em texto/PDF
        Path invoicePath = rootLocation
                .resolve("invoices")
                .resolve("invoice_" + order.getId() + ".txt");

        String content = invoiceService.buildInvoiceContent(order);
        Files.writeString(invoicePath, content);
    }
}
