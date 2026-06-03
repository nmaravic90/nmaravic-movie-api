package com.nmaravic.movie.api.util;

import com.nmaravic.movie.api.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageUtil {

    private static final String SEPARATOR = "/";

    private FileStorageUtil() {}

    public static void saveFile(String uploadDir, Long movieId, MultipartFile file, String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir, String.valueOf(movieId)).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new FileStorageException("Invalid file path detected");
            }

            Files.createDirectories(uploadPath);
            byte[] bytes = file.getBytes();
            Files.write(filePath, bytes);
        }
        catch (IOException e) {
            throw new FileStorageException("Failed to save image");
        }
    }

    public static void deleteFile(String uploadDir, String fileUrl, String serverBaseUrl) {
        try {
            String relativePath = fileUrl.replace(serverBaseUrl + SEPARATOR, "");
            Path filePath = Paths.get(uploadDir, relativePath.split(SEPARATOR)).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            throw new FileStorageException("Failed to delete image");
        }
    }

    public static void deleteMovieFolder(String uploadDir, Long movieId) {
        try {
            Path folderPath = Paths.get(uploadDir, String.valueOf(movieId)).toAbsolutePath().normalize();
            Files.deleteIfExists(folderPath);
        }
        catch (IOException e) {
            throw new FileStorageException("Failed to delete movie folder");
        }
    }
}
