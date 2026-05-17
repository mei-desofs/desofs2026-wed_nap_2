package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String subDir) throws StorageException;
    String uploadBytes(byte[] data, String filename, String subDir) throws StorageException;
    byte[] downloadFile(String path) throws StorageException;
    void deleteFile(String path) throws StorageException;
}
