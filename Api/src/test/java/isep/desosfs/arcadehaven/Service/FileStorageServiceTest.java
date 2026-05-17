package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import isep.desosfs.arcadehaven.Domain.Order;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {
    @Mock StorageService storageService;
    @Mock InvoiceService invoiceService;
    @InjectMocks FileStorageService service;

    @Test
    void shouldRejectInvalidFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveFile(file, "games/images"));
    }

    @Test
    void shouldRejectPathTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> service.downloadFile("../hack.txt"));
    }

    @Test
    void shouldSaveFileSuccessfully() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(storageService.uploadFile(file, "games/images")).thenReturn("games/images/image.png");

        String result = service.saveFile(file, "games/images");

        assertNotNull(result);
        assertTrue(result.contains("image.png"));
    }

    @Test
    void shouldGenerateInvoiceFile() throws Exception {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(UUID.randomUUID());
        when(invoiceService.buildInvoiceContent(order)).thenReturn("invoice content");
        when(storageService.uploadBytes(any(), any(), eq("invoices"))).thenReturn("invoices/invoice.txt");

        String result = service.generateInvoice(order);

        assertNotNull(result);
    }

    @Test
    void shouldDownloadFileSuccessfully() {
        when(storageService.downloadFile("invoices/a.txt"))
                .thenReturn("data".getBytes());

        byte[] result = service.downloadFile("invoices/a.txt");

        assertNotNull(result);
    }

    @Test
    void shouldDeleteFileSuccessfully() {
        service.deleteFile("invoices/a.txt");
    }

    @Test
    void shouldRejectEmptyPath() {
        assertThrows(IllegalArgumentException.class,
                () -> service.downloadFile(""));
    }

    @Test
    void shouldRejectNullPath() {
        assertThrows(IllegalArgumentException.class,
                () -> service.downloadFile(null));
    }

    @Test
    void shouldRejectTraversalPath() {
        assertThrows(IllegalArgumentException.class,
                () -> service.downloadFile("../evil.txt"));
    }

    @Test
    void shouldSaveActivationKeysFileSuccessfully() {
        Order order = mock(Order.class);

        when(order.getId()).thenReturn(UUID.randomUUID());
        when(order.getItems()).thenReturn(java.util.List.of());

        when(storageService.uploadBytes(any(), any(), eq("keys")))
                .thenReturn("keys/file.txt");

        String result = service.saveActivationKeysFile(order);

        assertNotNull(result);
    }
}
