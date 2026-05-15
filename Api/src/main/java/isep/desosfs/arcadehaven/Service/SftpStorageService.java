package isep.desosfs.arcadehaven.Service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class SftpStorageService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String baseRemoteDir;

    public SftpStorageService(
            @Value("${sftp.host}") String host,
            @Value("${sftp.port}") int port,
            @Value("${sftp.username}") String username,
            @Value("${sftp.password}") String password,
            @Value("${sftp.remote-dir}") String baseRemoteDir
    ) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.baseRemoteDir = baseRemoteDir;
    }

    //Used for short lived connections
    private SSHClient createClient() throws Exception {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());

        ssh.connect(host, port);
        ssh.authPassword(username, password);

        return ssh;
    }

    private void ensureDirExists(SFTPClient sftp, String path) throws Exception {
        String[] folders = path.split("/");
        StringBuilder current = new StringBuilder();

        for (String folder : folders) {
            if (folder.isBlank()) {
                continue;
            }

            current.append("/").append(folder);

            try {
                sftp.stat(current.toString());
            } catch (Exception e) {
                try {
                    sftp.mkdir(current.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String uploadFile(MultipartFile file, String subDir) throws Exception {
        SSHClient ssh = createClient();

        try (SFTPClient sftp = ssh.newSFTPClient();
            InputStream input = file.getInputStream()) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            String folder = baseRemoteDir + "/" + subDir;
            String remotePath = folder + "/" + filename;
            ensureDirExists(sftp, folder);

            try (RemoteFile remoteFile = sftp.open(
                    remotePath,
                    EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC)
            );
                 OutputStream out = remoteFile.new RemoteFileOutputStream()) {

                input.transferTo(out);
                out.flush();
            }

            return remotePath;
        } finally {
            try {
                ssh.disconnect();
            } catch (Exception ignored) {}
        }
    }

    public String uploadBytes(byte[] data, String filename, String subDir) throws Exception {
        SSHClient ssh = createClient();

        try (SFTPClient sftp = ssh.newSFTPClient()) {
            String folder = baseRemoteDir + "/" + subDir;
            String remotePath = folder + "/" + filename;

            ensureDirExists(sftp, folder);

            try (var remoteFile = sftp.open(remotePath, EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC))) {
                remoteFile.new RemoteFileOutputStream().write(data);
            }

            return remotePath;
        } finally {
            ssh.disconnect();
        }
    }

    public byte[] downloadFile(String remotePath) throws Exception {
        SSHClient ssh = createClient();

        try (SFTPClient sftp = ssh.newSFTPClient();
             var remoteFile = sftp.open(remotePath);
             InputStream in = remoteFile.new RemoteFileInputStream()) {

            return in.readAllBytes();
        } finally {
            ssh.disconnect();
        }
    }

    public void deleteFile(String remotePath) throws Exception {
        SSHClient ssh = createClient();

        try (SFTPClient sftp = ssh.newSFTPClient()) {
            sftp.rm(remotePath);
        } finally {
            ssh.disconnect();
        }
    }
}