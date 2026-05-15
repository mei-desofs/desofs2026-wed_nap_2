package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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
}
