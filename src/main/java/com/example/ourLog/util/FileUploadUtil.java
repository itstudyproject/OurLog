package com.example.ourLog.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${com.example.upload.path}")
    private String uploadDir;

    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        // 업로드 디렉토리 생성
        String dateStr = LocalDate.now().toString().replace("-", File.separator);
        Path uploadPath = Paths.get(uploadDir, "profile", dateStr);
        
        // 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 생성 (UUID + 원본 파일명)
        String originalFileName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + originalFileName;

        // 파일 저장 경로
        Path targetPath = uploadPath.resolve(savedFileName);
        
        // 파일 복사
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 상대 경로 반환 (데이터베이스 저장용)
        return Paths.get("profile", dateStr, savedFileName).toString().replace("\\", "/");
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        Files.deleteIfExists(path);
    }
} 