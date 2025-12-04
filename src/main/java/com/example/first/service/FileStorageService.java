package com.example.first.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("/home/Serverman/Spring/files/media/players"); // 원하는 경로

    public String save(MultipartFile file) {
        try {
            if (Files.notExists(root)) {
                Files.createDirectories(root);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = root.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // Nginx에서 /media → /var/www/media 연결
            return "/media/players/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("File saving failed", e);
        }
    }
}
