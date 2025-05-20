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

import com.example.ourLog.dto.UploadResultDTO;
import net.coobird.thumbnailator.Thumbnails;

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

    // 일반 파일 업로드 + 썸네일 생성 (폴더, 파일명, 썸네일 크기 지정)
    public UploadResultDTO uploadFile(MultipartFile file, String subDir, int thumbnailWidth, int thumbnailHeight) throws IOException {
        // 업로드 디렉토리 생성
        String dateStr = LocalDate.now().toString().replace("-", File.separator);
        Path uploadPath = Paths.get(uploadDir, subDir, dateStr);
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
        // 썸네일 파일명 및 경로
        String thumbnailFileName = "s_" + uuid + "_" + originalFileName;
        Path thumbnailPath = uploadPath.resolve(thumbnailFileName);
        // 썸네일 생성
        Thumbnails.of(targetPath.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .toFile(thumbnailPath.toFile());
        // 상대 경로 반환 (데이터베이스 저장용)
        String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
        return new UploadResultDTO(originalFileName, uuid, folderPath);
    }
} 