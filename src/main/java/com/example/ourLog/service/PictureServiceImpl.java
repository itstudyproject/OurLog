package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.util.FileUploadUtil;
import com.example.ourLog.dto.UploadResultDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class PictureServiceImpl implements PictureService {

  private final PictureRepository pictureRepository;
  private final FileUploadUtil fileUploadUtil;

  // application.properties 에 설정된 실제 업로드 루트 예: "C:/uploads"
  @Value("${com.example.upload.path}")
  private String uploadPath;

  @Override
  @Transactional
  public List<PictureDTO> uploadFiles(List<MultipartFile> files) {
    List<PictureDTO> resultList = new ArrayList<>();
    for (MultipartFile file : files) {
      try {
        // FileUploadUtil 에서 실제로 파일을 year/month/day/... 구조로 저장했다고 가정
        UploadResultDTO uploadResult = fileUploadUtil.uploadFile(file, "", 200, 200);

        Picture picture = Picture.builder()
            .uuid(uploadResult.getUuid())
            .picName(uploadResult.getFileName())
            .path(uploadResult.getFolderPath()) // ex: "2025/06/02"
            .originImagePath(uploadResult.getFolderPath() + "/" + uploadResult.getUuid() + "_" + uploadResult.getFileName())
            .thumbnailImagePath(uploadResult.getFolderPath() + "/s_" + uploadResult.getUuid() + "_" + uploadResult.getFileName())
            .resizedImagePath(uploadResult.getFolderPath() + "/m_" + uploadResult.getUuid() + "_" + uploadResult.getFileName())
            .post(null) // 일단 Post 연결은 나중에 assignPicturesToPost 에서 처리
            .downloads(0L)
            .build();

        pictureRepository.save(picture);

        PictureDTO dto = PictureDTO.builder()
            .uuid(picture.getUuid())
            .picName(picture.getPicName())
            .path(picture.getPath())
            .originImagePath(picture.getOriginImagePath())
            .thumbnailImagePath(picture.getThumbnailImagePath())
            .resizedImagePath(picture.getResizedImagePath())
            .build();

        resultList.add(dto);

      } catch (IOException e) {
        log.error("파일 저장 실패: {}", e.getMessage());
      }
    }
    return resultList;
  }

  @Override
  public PictureDTO getPictureById(Long picId) {
    Optional<Picture> result = pictureRepository.findById(picId);
    if (result.isPresent()) {
      return entityToDTO(result.get());
    }
    return null;
  }

  // ─────────────────────────────────────────────────────────────
  // 삭제 메서드 (최종적으로 Picture 엔티티와 파일을 함께 삭제)
  @Override
  @Transactional
  public void deletePictureById(Long picId) {
    // 1) DB에서 엔티티 조회
    Picture picture = pictureRepository.findById(picId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 picId: " + picId));

    // 2) 파일 시스템에서 origin, thumbnail, resized 이미지 삭제
    // picture.getPath() → ex: "2025/06/02"
    // picture.getOriginImagePath() → ex: "2025/06/02/uuid_filename.png"
    // picture.getThumbnailImagePath() → ex: "2025/06/02/s_uuid_filename.png"
    // picture.getResizedImagePath() → ex: "2025/06/02/m_uuid_filename.png"
    deleteFileIfExists(picture.getOriginImagePath());
    deleteFileIfExists(picture.getThumbnailImagePath());
    deleteFileIfExists(picture.getResizedImagePath());

    // 3) DB 레코드 삭제
    pictureRepository.delete(picture);
  }

  // 실제 파일 경로(uploadPath + "/" + 상대경로) 존재 시 삭제
  private void deleteFileIfExists(String relativePath) {
    if (relativePath == null || relativePath.isEmpty()) {
      return;
    }
    Path filePath = Paths.get(uploadPath, relativePath);
    try {
      File file = filePath.toFile();
      if (file.exists()) {
        Files.deleteIfExists(filePath);
        log.info("파일 삭제 성공: {}", filePath);
      }
    } catch (IOException e) {
      log.warn("파일 삭제 중 오류 발생: {} → {}", filePath, e.getMessage());
    }
  }
  // ─────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public void assignPicturesToPost(List<String> uuids, Long postId) {
    for (String uuid : uuids) {
      Picture picture = pictureRepository.findByUuid(uuid);
      if (picture != null && picture.getPost() == null) {
        picture.setPost(Post.builder().postId(postId).build());
        pictureRepository.save(picture);
      }
    }
  }

  private String makeFolder() {
    String dateStr = LocalDate.now().toString().replace("-", File.separator);
    File uploadDir = new File(uploadPath, dateStr);
    if (!uploadDir.exists()) {
      uploadDir.mkdirs();
    }
    return dateStr;
  }

  // ▶ 필요하다면 아래 메서드를 PictureDTO 변환 로직에 맞춰 구현
  public PictureDTO entityToDTO(Picture e) {
    return PictureDTO.builder()
        .uuid(e.getUuid())
        .picName(e.getPicName())
        .path(e.getPath())
        .originImagePath(e.getOriginImagePath())
        .thumbnailImagePath(e.getThumbnailImagePath())
        .resizedImagePath(e.getResizedImagePath())
        .build();
  }

}
