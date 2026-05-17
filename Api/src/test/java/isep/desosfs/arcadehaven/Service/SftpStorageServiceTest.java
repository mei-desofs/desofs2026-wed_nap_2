package isep.desosfs.arcadehaven.Service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.mock.web.MockMultipartFile;

import isep.desosfs.arcadehaven.Exception.StorageException;
import net.schmizz.sshj.SSHClient;

class SftpStorageServiceTest {

    private SftpStorageService service;

    @BeforeEach
    void setUp() {
        service = new SftpStorageService("localhost", 22, "user", "pass", "/remote", "");
    }

    @Test
    void uploadFile_sshConnectFails_throwsStorageException() {
        try (MockedConstruction<SSHClient> ignored =
                mockConstruction(SSHClient.class, (mock, ctx) ->
                        doThrow(new IOException("refused")).when(mock).connect(anyString(), anyInt()))) {
            MockMultipartFile file =
                    new MockMultipartFile("file", "img.png", "image/png", new byte[]{1, 2, 3});
            assertThatThrownBy(() -> service.uploadFile(file, "games/images"))
                    .isInstanceOf(StorageException.class);
        }
    }

    @Test
    void uploadBytes_sshConnectFails_throwsStorageException() {
        try (MockedConstruction<SSHClient> ignored =
                mockConstruction(SSHClient.class, (mock, ctx) ->
                        doThrow(new IOException("refused")).when(mock).connect(anyString(), anyInt()))) {
            assertThatThrownBy(() -> service.uploadBytes(new byte[]{1, 2, 3}, "file.txt", "invoices"))
                    .isInstanceOf(StorageException.class);
        }
    }

    @Test
    void downloadFile_sshConnectFails_throwsStorageException() {
        try (MockedConstruction<SSHClient> ignored =
                mockConstruction(SSHClient.class, (mock, ctx) ->
                        doThrow(new IOException("refused")).when(mock).connect(anyString(), anyInt()))) {
            assertThatThrownBy(() -> service.downloadFile("/remote/invoices/file.txt"))
                    .isInstanceOf(StorageException.class);
        }
    }

    @Test
    void deleteFile_sshConnectFails_throwsStorageException() {
        try (MockedConstruction<SSHClient> ignored =
                mockConstruction(SSHClient.class, (mock, ctx) ->
                        doThrow(new IOException("refused")).when(mock).connect(anyString(), anyInt()))) {
            assertThatThrownBy(() -> service.deleteFile("/remote/invoices/file.txt"))
                    .isInstanceOf(StorageException.class);
        }
    }

    @Test
    void validateAndEnsureDirs_sshConnectFails_doesNotThrow() {
        try (MockedConstruction<SSHClient> ignored =
                mockConstruction(SSHClient.class, (mock, ctx) ->
                        doThrow(new IOException("refused")).when(mock).connect(anyString(), anyInt()))) {
            assertThatCode(() -> service.validateAndEnsureDirs()).doesNotThrowAnyException();
        }
    }

    @Test
    void uploadFile_knownHostsFileNotFound_throwsStorageExceptionWithMessage() {
        SftpStorageService svcWithKnownHosts = new SftpStorageService(
                "localhost", 22, "user", "pass", "/remote", "/non/existent/known_hosts");
        MockMultipartFile file =
                new MockMultipartFile("file", "img.png", "image/png", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> svcWithKnownHosts.uploadFile(file, "games/images"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Cannot load SFTP known-hosts");
    }
}
