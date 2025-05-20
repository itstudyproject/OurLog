package com.example.ourLog.controller;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.service.PictureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/picture")
@Log4j2
public class PictureController {

  private final PictureService pictureService;

  @Value("${com.example.upload.path}")
  private String uploadPath; // ✅ 실제 파일 시스템 경로

  @PostMapping("/upload")
  public ResponseEntity<List<PictureDTO>> upload(@RequestPart("files") List<MultipartFile> files) {
    List<PictureDTO> result = pictureService.uploadFiles(files);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/get/{picId}")
  public ResponseEntity<PictureDTO> getPictureById(@PathVariable Long picId) {
    log.info("get picture for picId: {}", picId);
    PictureDTO result = pictureService.getPictureById(picId);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/display/{year}/{month}/{day}/{fileName:.+}")
  public ResponseEntity<FileSystemResource> displayImage(
      @PathVariable String year,
      @PathVariable String month,
      @PathVariable String day,
      @PathVariable String fileName) {

    try {
      // ✅ 실제 경로 조합
      Path filePath = Paths.get(uploadPath, year, month, day, fileName);
      FileSystemResource resource = new FileSystemResource(filePath.toFile());

      if (!resource.exists()) {
        log.warn("File not found: {}", filePath);
        return ResponseEntity.notFound().build();
      }

      // ✅ MIME 타입 추론
      String contentType = resolveContentType(fileName);

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .body(resource);

    } catch (Exception e) {
      log.error("Image load error: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // 🔍 파일 확장자에 따른 MIME 타입 결정
  private String resolveContentType(String fileName) {
    String lowered = fileName.toLowerCase();
    if (lowered.endsWith(".png")) return "image/png";
    if (lowered.endsWith(".jpg") || lowered.endsWith(".jpeg")) return "image/jpeg";
    if (lowered.endsWith(".gif")) return "image/gif";
    if (lowered.endsWith(".webp")) return "image/webp";
    return "application/octet-stream";
  }
}
