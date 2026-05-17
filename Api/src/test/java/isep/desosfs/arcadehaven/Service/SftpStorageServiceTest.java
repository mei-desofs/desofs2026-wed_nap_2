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
import net.schmizz.sshj.sftp.SFTPClient;

import static org.mockito.Mockito.*;

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

    @Test
    void validateAndEnsureDirs_success_createsDirectories() throws Exception {

        try (MockedConstruction<SSHClient> ignored =
                     mockConstruction(
                             SSHClient.class,
                             (mock, ctx) -> {

                                 SFTPClient sftp =
                                         mock(SFTPClient.class);

                                 when(mock.newSFTPClient())
                                         .thenReturn(sftp);

                                 doThrow(new RuntimeException())
                                         .when(sftp)
                                         .stat(anyString());
                             })) {

            assertThatCode(() ->
                    service.validateAndEnsureDirs())
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void uploadBytes_sftpFails_throwsStorageException() throws Exception {

        try (MockedConstruction<SSHClient> ignored =
                     mockConstruction(
                             SSHClient.class,
                             (mock, ctx) -> {

                                 when(mock.newSFTPClient())
                                         .thenThrow(
                                                 new IOException("SFTP error")
                                         );
                             })) {

            assertThatThrownBy(() ->
                    service.uploadBytes(
                            new byte[]{1},
                            "file.txt",
                            "keys"))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("Failed to upload bytes");
        }
    }

    @Test
    void downloadFile_sftpFails_throwsStorageException() throws Exception {

        try (MockedConstruction<SSHClient> ignored =
                     mockConstruction(
                             SSHClient.class,
                             (mock, ctx) -> {

                                 when(mock.newSFTPClient())
                                         .thenThrow(
                                                 new IOException("SFTP error")
                                         );
                             })) {

            assertThatThrownBy(() ->
                    service.downloadFile("/remote/file.txt"))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("Failed to download file");
        }
    }

    @Test
    void deleteFile_sftpFails_throwsStorageException() throws Exception {

        try (MockedConstruction<SSHClient> ignored =
                     mockConstruction(
                             SSHClient.class,
                             (mock, ctx) -> {

                                 when(mock.newSFTPClient())
                                         .thenThrow(
                                                 new IOException("SFTP error")
                                         );
                             })) {

            assertThatThrownBy(() ->
                    service.deleteFile("/remote/file.txt"))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("Failed to delete file");
        }
    }

    @Test
    void validateAndEnsureDirs_directoryAlreadyExists_doesNotThrow() throws Exception {
        try (MockedConstruction<SSHClient> ignored =
                        mockConstruction(SSHClient.class, (mock, ctx) -> {

                        SFTPClient sftp = mock(SFTPClient.class);
                        when(mock.newSFTPClient()).thenReturn(sftp);

                        doNothing().when(sftp).stat(anyString());

                        })) {

                SftpStorageService svc =
                        new SftpStorageService("localhost", 22, "user", "pass", "/remote", "");

                assertThatCode(() -> svc.validateAndEnsureDirs())
                        .doesNotThrowAnyException();
        }
    }
}
