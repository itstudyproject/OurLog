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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class PictureServiceImpl implements PictureService {

  private final PictureRepository pictureRepository;
  private final FileUploadUtil fileUploadUtil;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @Override
  @Transactional
  public List<PictureDTO> uploadFiles(List<MultipartFile> files) {
    List<PictureDTO> resultList = new ArrayList<>();
    for (MultipartFile file : files) {
      try {

        UploadResultDTO uploadResult = fileUploadUtil.uploadFile(file, "", 200, 200);
        Picture picture = Picture.builder()
            .uuid(uploadResult.getUuid())
            .picName(uploadResult.getFileName())
            .path(uploadResult.getFolderPath())
            .originImagePath(uploadResult.getFolderPath() + "/" + uploadResult.getUuid() + "_" + uploadResult.getFileName())
            .thumbnailImagePath(uploadResult.getFolderPath() + "/s_" + uploadResult.getUuid() + "_" + uploadResult.getFileName())
            .resizedImagePath(uploadResult.getFolderPath() + "/m_" + uploadResult.getUuid() + "_" + uploadResult.getFileName())
            .post(null)
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
    if (result.isPresent()) return entityToDTO(result.get());
    return null;
  }

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
}
