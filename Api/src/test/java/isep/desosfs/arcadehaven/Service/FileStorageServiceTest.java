package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import isep.desosfs.arcadehaven.Domain.Order;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {
    @Mock InvoiceService invoiceService;

    private FileStorageService service;

    @BeforeEach
    void setup() {
        service = new FileStorageService("test-storage", invoiceService);
        service.init();
    }

    @Test
    void shouldRejectInvalidFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveFile(file, "games/images"));
    }

    @Test
    void shouldRejectPathTraversal() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("../hack.png");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveFile(file, "games/images"));
    }

    @Test
    void shouldSaveFileSuccessfully() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream("data".getBytes())
        );

        String result = service.saveFile(file, "games/images");

        assertNotNull(result);
        assertTrue(result.contains("image.png"));
    }

    @Test
    void shouldInitializeDirectories() {
        service.init();
    }

    @Test
    void shouldGenerateInvoiceFile() throws Exception {
        Order order = mock(Order.class);

        when(order.getId()).thenReturn(UUID.randomUUID());
        when(invoiceService.buildInvoiceContent(order)).thenReturn("invoice");

        service.generateInvoice(order);
    }
}
