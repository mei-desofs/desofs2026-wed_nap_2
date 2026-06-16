package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.StorageException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.backend", havingValue = "sftp", matchIfMissing = true)
public class SftpStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(SftpStorageService.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String baseRemoteDir;
    private final String knownHostsPath;

    public SftpStorageService(
            @Value("${sftp.host}") String host,
            @Value("${sftp.port}") int port,
            @Value("${sftp.username}") String username,
            @Value("${sftp.password}") String password,
            @Value("${sftp.remote-dir}") String baseRemoteDir,
            // ASVS V12.3.4 — path to SSH known_hosts file; blank = dev-only PromiscuousVerifier
            @Value("${sftp.known-hosts-path:}") String knownHostsPath
    ) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.baseRemoteDir = baseRemoteDir;
        this.knownHostsPath = knownHostsPath;
    }

    // RF-31 / RF-32 — verify SFTP connectivity and pre-create required directories at startup
    @EventListener(ApplicationReadyEvent.class)
    public void validateAndEnsureDirs() {
        SSHClient ssh;
        try {
            ssh = createClient();
        } catch (StorageException e) {
            log.error("SFTP startup check FAILED — storage operations will fail at runtime: {}", e.getMessage());
            return;
        }
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            for (String sub : List.of("invoices", "keys", "games/images")) {
                ensureDirExists(sftp, baseRemoteDir + "/" + sub);
            }
            log.info("SFTP connectivity OK — required directories verified at {}", baseRemoteDir);
        } catch (Exception e) {
            log.error("SFTP directory setup FAILED: {}", e.getMessage());
        } finally {
            disconnectQuietly(ssh);
        }
    }

    private SSHClient createClient() throws StorageException {
        try {
            SSHClient ssh = new SSHClient();
            // ASVS V12.3.4 — use known-hosts file when configured; PromiscuousVerifier is
            // only acceptable inside a trusted Docker-internal network (dev / CI).
            if (knownHostsPath != null && !knownHostsPath.isBlank()) {
                ssh.addHostKeyVerifier(new OpenSSHKnownHosts(new File(knownHostsPath)));
            } else {
                log.warn("SFTP host-key verification is DISABLED (PromiscuousVerifier). " +
                         "Set sftp.known-hosts-path for production deployments.");
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
            }
            ssh.connect(host, port);
            ssh.authPassword(username, password);
            return ssh;
        } catch (IOException e) {
            throw new StorageException("Cannot load SFTP known-hosts from " + knownHostsPath, e);
        } catch (Exception e) {
            throw new StorageException("Cannot connect to SFTP server at " + host + ":" + port, e);
        }
    }

    private void ensureDirExists(SFTPClient sftp, String path) {
        String[] folders = path.split("/");
        StringBuilder current = new StringBuilder();
        for (String folder : folders) {
            if (folder.isBlank()) continue;
            current.append("/").append(folder);
            try {
                sftp.stat(current.toString());
            } catch (Exception e) {
                try {
                    sftp.mkdir(current.toString());
                } catch (Exception ex) {
                    log.warn("Could not create remote directory '{}': {}", current, ex.getMessage());
                }
            }
        }
    }

    public String uploadFile(MultipartFile file, String subDir) throws StorageException {
        validateSubDir(subDir); // V1.3.3 / V5.3.2
        SSHClient ssh = createClient();
        try (SFTPClient sftp = ssh.newSFTPClient();
             InputStream input = file.getInputStream()) {

            String originalFilename = file.getOriginalFilename();
            String safeFilename = sanitizeFilename(originalFilename);

            String filename = UUID.randomUUID() + "_" + safeFilename;
            String folder = baseRemoteDir + "/" + subDir;
            String absolutePath = folder + "/" + filename;
            ensureDirExists(sftp, folder);

            try (RemoteFile remoteFile = sftp.open(absolutePath,
                         EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC));
                 OutputStream out = remoteFile.new RemoteFileOutputStream()) {
                input.transferTo(out);
                out.flush();
            }
            return subDir + "/" + filename;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to SFTP: " + e.getMessage(), e);
        } finally {
            disconnectQuietly(ssh);
        }
    }

    // V1.3.3 / V5.3.2 — reject sub-directory paths that could escape the base directory
    private void validateSubDir(String subDir) {
        if (subDir == null || subDir.isBlank()) {
            throw new StorageException("Sub-directory must not be empty");
        }
        String normalized = subDir.replace("\\", "/");
        if (normalized.startsWith("/") || normalized.contains("..")) {
            throw new StorageException("Sub-directory contains path traversal sequence: " + subDir);
        }
    }

    // V1.3.3
    private String sanitizeFilename(String input) {
        if (input == null) return "file";
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public String uploadBytes(byte[] data, String filename, String subDir) throws StorageException {
        validateSubDir(subDir); // V1.3.3 / V5.3.2
        String safeFilename = sanitizeFilename(filename); // V1.3.3 — defence-in-depth
        SSHClient ssh = createClient();
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            String folder = baseRemoteDir + "/" + subDir;
            String absolutePath = folder + "/" + safeFilename;
            ensureDirExists(sftp, folder);

            try (RemoteFile remoteFile = sftp.open(absolutePath,
                         EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC));
                 OutputStream out = remoteFile.new RemoteFileOutputStream()) {
                out.write(data);
                out.flush();
            }
            return subDir + "/" + safeFilename;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Failed to upload bytes to SFTP: " + e.getMessage(), e);
        } finally {
            disconnectQuietly(ssh);
        }
    }

    @Override
    public byte[] downloadFile(String path) throws StorageException {
        String absolutePath = resolveAbsolutePath(path); // V1.3.3 / V5.3.2
        SSHClient ssh = createClient();
        try (SFTPClient sftp = ssh.newSFTPClient();
             RemoteFile remoteFile = sftp.open(absolutePath);
             InputStream in = remoteFile.new RemoteFileInputStream()) {
            return in.readAllBytes();
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Failed to download file from SFTP: " + absolutePath, e);
        } finally {
            disconnectQuietly(ssh);
        }
    }

    public void deleteFile(String path) throws StorageException {
        String absolutePath = resolveAbsolutePath(path); // V1.3.3 / V5.3.2
        SSHClient ssh = createClient();
        try (SFTPClient sftp = ssh.newSFTPClient()) {
            sftp.rm(absolutePath);
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Failed to delete file from SFTP: " + absolutePath, e);
        } finally {
            disconnectQuietly(ssh);
        }
    }

    // V1.3.3 / V5.3.2 — resolve a relative path against baseRemoteDir; rejects traversals
    private String resolveAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new StorageException("Remote path must not be empty");
        }
        if (relativePath.contains("..")) {
            throw new StorageException("Remote path contains traversal sequence: " + relativePath);
        }
        String base = baseRemoteDir.replace("\\", "/").replaceAll("/$", "");
        String rel  = relativePath.replace("\\", "/");
        String absolute = base + "/" + rel;
        // Normalise double slashes and verify the result is still under base
        absolute = absolute.replaceAll("/+", "/");
        if (!absolute.startsWith(base + "/")) {
            throw new StorageException("Remote path escapes permitted base directory: " + relativePath);
        }
        return absolute;
    }

    private void disconnectQuietly(SSHClient ssh) {
        try {
            ssh.disconnect();
        } catch (Exception ignored) {
        }
    }
}
