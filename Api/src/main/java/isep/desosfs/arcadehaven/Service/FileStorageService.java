package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Order;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public String saveFile(MultipartFile file, String subdir) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        return sftpStorageService.uploadFile(file, subdir);
    }

    public byte[] downloadFile(String remotePath) throws Exception {
        return sftpStorageService.downloadFile(remotePath);
    }

    public void deleteFile(String remotePath) throws Exception {
        sftpStorageService.deleteFile(remotePath);
    }

    public String generateInvoice(Order order) throws Exception {
        String content = invoiceService.buildInvoiceContent(order);

        String filename = "invoice_" + order.getId() + "_" + UUID.randomUUID() + ".txt";

        return sftpStorageService.uploadBytes(content.getBytes(StandardCharsets.UTF_8), filename, "invoices");
    }
}
