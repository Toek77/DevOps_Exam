package net.orderzone.idcard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class PhotoStorageService {

    @Value("${app.photo.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();

        if (!"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)) {
            throw new RuntimeException("Only JPEG and PNG files are allowed");
        }

        String fileName =
                UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(
                file.getInputStream(),
                uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        return fileName;
    }

    public Resource load(String filename) throws Exception {

        Path filePath =
                Paths.get(uploadDir)
                        .resolve(filename);

        return new UrlResource(filePath.toUri());
    }

    public void delete(String filename) throws Exception {

        Path filePath =
                Paths.get(uploadDir)
                        .resolve(filename);

        Files.deleteIfExists(filePath);
    }
}