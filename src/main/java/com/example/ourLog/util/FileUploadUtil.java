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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Component
public class FileUploadUtil {

  private final String uploadDir;
  // 컨텍스트 패스를 주입받을 필드
  private final String contextPath;
  private static final Logger log = LoggerFactory.getLogger(FileUploadUtil.class);

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
    // 업로드된 파일 정보 로깅
    log.info("[{}] ➡️ uploadProfileImage 호출됨. 파일명: {}, Content Type: {}, 파일 크기: {}",
        MDC.get("requestId"), file.getOriginalFilename(), file.getContentType(), file.getSize());

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
    // Content Type이 image/* 이거나, Content Type이 application/octet-stream이지만 이미지 파일 확장자를 가지는 경우
    try {
      // 썸네일 생성 시도 로그
      log.info("[{}] ➡️ 썸네일 생성 시도. 원본: {}, 대상: {}, 크기: {}x{}",
          MDC.get("requestId"), targetPath, thumbnailPath, thumbnailWidth, thumbnailHeight);

      Thumbnails.of(targetPath.toFile())
          .size(thumbnailWidth, thumbnailHeight)
          .toFile(thumbnailPath.toFile());

      // 썸네일 생성 성공 로그
      log.info("[{}] ✅ 썸네일 생성 성공: {}", MDC.get("requestId"), thumbnailPath);

    } catch (Exception e) {
      // 썸네일 생성 실패 로그
      log.error("[{}] ❌ 썸네일 생성 실패. 원본: {}, 대상: {}", MDC.get("requestId"), targetPath, thumbnailPath, e);
      e.printStackTrace();
      // 썸네일 생성 실패 시 함수를 종료하지 않고 로그만 남깁니다.
    }

    // 원본 파일 저장 성공 또는 썸네일 생성 성공/실패와 관계없이 최종적으로 UploadResultDTO를 생성하여 반환
    String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
    // UploadResultDTO 생성 시 컨텍스트 패스 전달
    return new UploadResultDTO(originalFileName, uuid, folderPath, this.contextPath); // 이 return 문으로 도달하게 합니다.
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

    try { // 썸네일 생성 및 중간 크기 이미지 생성에 예외 처리를 추가 (원래 코드는 없었음)
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
    } catch (Exception e) {
      e.printStackTrace();
      // uploadFile 메서드에서도 썸네일 생성 실패 시 로그만 남기고 함수 종료는 하지 않도록 수정
    }

    String folderPath = Paths.get(subDir, dateStr).toString().replace("\\", "/");
    // UploadResultDTO 생성 시 컨텍스트 패스 전달
    return new UploadResultDTO(originalFileName, uuid, folderPath, this.contextPath); // this.contextPath 추가
  }
}