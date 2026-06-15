package isep.desosfs.arcadehaven.Security;

import isep.desosfs.arcadehaven.Exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class ClamAVService {

    @Value("${clamav.host}")
    private String host;

    @Value("${clamav.port}")
    private int port;

    @Value("${clamav.timeout-ms}")
    private int timeout;

    public void scan(MultipartFile file) {
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(timeout);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Start command
            out.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));

            InputStream fileStream = file.getInputStream();

            byte[] buffer = new byte[2048];
            int read;

            while ((read = fileStream.read(buffer)) != -1) {
                byte[] size = ByteBuffer.allocate(4).putInt(read).array();

                out.write(size);
                out.write(buffer, 0, read);
            }

            // End stream
            out.write(new byte[]{0,0,0,0});
            out.flush();

            String response = readResponse(in);

            if (response.contains("FOUND")) {
                throw new StorageException("Virus detected in uploaded file: " + response);
            }

            if (!response.contains("OK")) {
                throw new StorageException("Unexpected AV response: " + response);
            }

        } catch (IOException e) {
            throw new StorageException("ClamAV scan failed", e);
        }
    }

    private String readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int r;

        while ((r = in.read(buffer)) != -1) {
            baos.write(buffer, 0, r);
        }

        return baos.toString(StandardCharsets.UTF_8).trim();
    }
}