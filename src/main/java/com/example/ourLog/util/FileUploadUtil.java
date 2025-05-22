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

    private final String uploadDir;
    // 컨텍스트 패스를 주입받을 필드
    private final String contextPath;

    // 생성자 주입: uploadDir과 contextPath 모두 주입받습니다.
    public FileUploadUtil(
            @Value("${com.example.upload.path}") String uploadDir,
            @Value("${server.servlet.context-path}") String contextPath) {
        this.uploadDir = uploadDir;
        // 컨텍스트 패스가 루트("/")인 경우 빈 문자열로 처리하여 URL이 //profile/... 형태가 되지 않도록 합니다.
        this.contextPath = contextPath.equals("/") ? "" : contextPath;
    }

    // 기존 uploadProfileImage 메서드 수정
    public UploadResultDTO uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        String subDir = "profile";
        int thumbnailWidth = 100;
        int thumbnailHeight = 100;

        String dateStr = LocalDate.now().toString().replace("-", File.separator);
        Path uploadPath = Paths.get(uploadDir, subDir, dateStr);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();

        String savedFileName = uuid + "_" + originalFileName;
        Path targetPath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String thumbnailFileName = "s_" + uuid + "_" + originalFileName;
        Path thumbnailPath = uploadPath.resolve(thumbnailFileName);

        // 이미지 파일인 경우에만 썸네일 생성 시도
        if (file.getContentType() != null && file.getContentType().startsWith("image")) {
            try {
                Thumbnails.of(targetPath.toFile())
                        .size(thumbnailWidth, thumbnailHeight)
                        .toFile(thumbnailPath.toFile());
            } catch (Exception e) {
                e.printStackTrace();
                // 썸네일 생성 실패 시 원본 파일 정보와 함께 컨텍스트 패스 반환
                String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
                // UploadResultDTO 생성 시 컨텍스트 패스 전달
                return new UploadResultDTO(originalFileName, uuid, folderPath, this.contextPath); // this.contextPath 추가
            }
        } else {
            // 이미지 파일이 아닌 경우 썸네일 생성 건너뛰고 원본 파일 정보와 함께 컨텍스트 패스 반환
            String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
            // UploadResultDTO 생성 시 컨텍스트 패스 전달
            return new UploadResultDTO(originalFileName, uuid, folderPath, this.contextPath); // this.contextPath 추가
        }


        String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
        // UploadResultDTO 생성 시 컨텍스트 패스 전달
        return new UploadResultDTO(originalFileName, uuid, folderPath, this.contextPath); // this.contextPath 추가
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        Files.deleteIfExists(path);
    }

    // 기존 uploadFile 메서드도 필요하다면 UploadResultDTO 생성 시 컨텍스트 패스를 전달하도록 수정
    public UploadResultDTO uploadFile(MultipartFile file, String subDir, int thumbnailWidth, int thumbnailHeight) throws IOException {
        // ... 기존 uploadFile 로직 ...
        String dateStr = LocalDate.now().toString().replace("-", File.separator);
        Path uploadPath = Paths.get(uploadDir, subDir, dateStr);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFileName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + originalFileName;
        Path targetPath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        if (file.getContentType() != null && file.getContentType().startsWith("image")) {
            // 썸네일 생성
            String thumbnailFileName = "s_" + uuid + "_" + originalFileName;
            Path thumbnailPath = uploadPath.resolve(thumbnailFileName);
            Thumbnails.of(targetPath.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .toFile(thumbnailPath.toFile());

            // 중간 크기 이미지 생성 (700x700 고정 크기)
            String resizedFileName = "m_" + uuid + "_" + originalFileName;
            Path resizedPath = uploadPath.resolve(resizedFileName);
            Thumbnails.of(targetPath.toFile())
                .size(700, 700)
                .toFile(resizedPath.toFile());
        }

        String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
        // UploadResultDTO 생성 시 컨텍스트 패스 전달
        return new UploadResultDTO(originalFileName, uuid, folderPath, this.contextPath); // this.contextPath 추가
    }
}