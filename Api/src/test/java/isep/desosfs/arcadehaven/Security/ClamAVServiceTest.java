package isep.desosfs.arcadehaven.Security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import isep.desosfs.arcadehaven.Exception.StorageException;

class ClamAVServiceTest {

    private static final byte[] SMALL_FILE = new byte[]{1, 2, 3, 4, 5};

    private ClamAVService service;

    @BeforeEach
    void setUp() {
        service = new ClamAVService();
        ReflectionTestUtils.setField(service, "host", "localhost");
        ReflectionTestUtils.setField(service, "port", 3310);
        ReflectionTestUtils.setField(service, "timeout", 1000);
    }

    @Test
    void scan_cleanFile_doesNotThrow() throws IOException {
        byte[] okResponse = "stream: OK\0".getBytes(StandardCharsets.UTF_8);

        try (MockedConstruction<Socket> ignored = mockConstruction(Socket.class, (socket, ctx) -> {
            when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
            when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(okResponse));
        })) {
            MockMultipartFile file = new MockMultipartFile("f", "clean.png", "image/png", SMALL_FILE);
            assertDoesNotThrow(() -> service.scan(file));
        }
    }

    @Test
    void scan_virusDetected_throwsStorageExceptionWithVirusMessage() throws IOException {
        byte[] foundResponse = "stream: Win.Test.EICAR_HDB-1 FOUND\0".getBytes(StandardCharsets.UTF_8);

        try (MockedConstruction<Socket> ignored = mockConstruction(Socket.class, (socket, ctx) -> {
            when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
            when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(foundResponse));
        })) {
            MockMultipartFile file = new MockMultipartFile("f", "eicar.txt", "text/plain", SMALL_FILE);
            StorageException ex = assertThrows(StorageException.class, () -> service.scan(file));
            assertTrue(ex.getMessage().contains("Virus detected"),
                    "Message should say 'Virus detected', got: " + ex.getMessage());
        }
    }

    @Test
    void scan_unexpectedAvResponse_throwsStorageException() throws IOException {
        byte[] errorResponse = "stream: ERROR\0".getBytes(StandardCharsets.UTF_8);

        try (MockedConstruction<Socket> ignored = mockConstruction(Socket.class, (socket, ctx) -> {
            when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
            when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(errorResponse));
        })) {
            MockMultipartFile file = new MockMultipartFile("f", "test.png", "image/png", SMALL_FILE);
            StorageException ex = assertThrows(StorageException.class, () -> service.scan(file));
            assertTrue(ex.getMessage().contains("Unexpected AV response"),
                    "Message should say 'Unexpected AV response', got: " + ex.getMessage());
        }
    }

    @Test
    void scan_ioExceptionOnOutputStream_throwsStorageException() throws IOException {
        try (MockedConstruction<Socket> ignored = mockConstruction(Socket.class, (socket, ctx) -> {
            when(socket.getOutputStream()).thenThrow(new IOException("Connection error"));
        })) {
            MockMultipartFile file = new MockMultipartFile("f", "test.png", "image/png", SMALL_FILE);
            StorageException ex = assertThrows(StorageException.class, () -> service.scan(file));
            assertTrue(ex.getMessage().contains("ClamAV scan failed"),
                    "Message should say 'ClamAV scan failed', got: " + ex.getMessage());
        }
    }

    @Test
    void scan_largeFile_splitsIntoChunks() throws IOException {
        byte[] okResponse = "stream: OK\0".getBytes(StandardCharsets.UTF_8);
        byte[] largeContent = new byte[5000]; // larger than 2048-byte buffer

        try (MockedConstruction<Socket> ignored = mockConstruction(Socket.class, (socket, ctx) -> {
            when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
            when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(okResponse));
        })) {
            MockMultipartFile file = new MockMultipartFile("f", "big.png", "image/png", largeContent);
            assertDoesNotThrow(() -> service.scan(file));
        }
    }
}
