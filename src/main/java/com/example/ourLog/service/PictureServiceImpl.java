package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.repository.PictureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class PictureServiceImpl implements PictureService {

 private final PictureRepository pictureRepository;

 @Value("${com.example.upload.path}")
 private String uploadPath;

 // ✅ 1. 그림 파일 업로드
 @Transactional
 @Override
 public List<PictureDTO> uploadFiles(List<MultipartFile> files) {
   List<PictureDTO> resultList = new ArrayList<>();

   String folderPath = makeFolder(); // 예: 2024/04/18

   for (MultipartFile file : files) {
     String originalName = file.getOriginalFilename();
     String uuid = UUID.randomUUID().toString();
     String saveName = uuid + "_" + originalName;

     File saveFile = new File(uploadPath + File.separator + folderPath, saveName);
     try {
       file.transferTo(saveFile);

       // 썸네일 생성
       File thumbnailFile = new File(saveFile.getParent(), "s_" + saveName);
       Thumbnails.of(saveFile)
           .size(200, 200)
           .toFile(thumbnailFile);

       // DB 저장 (postId는 null로)
       Picture picture = Picture.builder()
           .uuid(uuid)
           .picName(originalName)
           .path(folderPath)
           .originImagePath(folderPath + "/" + saveName)
           .thumbnailImagePath(folderPath + "/s_" + saveName)
           .postId(null)
           .build();
       pictureRepository.save(picture);

       // 반환용 DTO
       PictureDTO dto = PictureDTO.builder()
           .uuid(uuid)
           .picName(originalName)
           .path(folderPath)
           .build();
       resultList.add(dto);
     } catch (IOException e) {
       log.error("파일 업로드 실패: {}", e.getMessage());
     }
   }

   return resultList;
 }

 // ✅ 2. 그림들을 Post에 연결
 @Transactional
 @Override
 public void assignPicturesToPost(Long postId) {
   for (Long postId : postId) {
     List<Picture> picture = pictureRepository.findByPostId(postId);
     if (picture != null && picture.getPostId() == null) {
       picture.setPostId(Post.builder().postId(postId).build());
       pictureRepository.save(picture);
     }
   }
 }

 // ✅ 폴더 생성 (날짜 기준)
 private String makeFolder() {
   String dateStr = LocalDate.now().toString().replace("-", File.separator);
   File uploadDir = new File(uploadPath, dateStr);
   if (!uploadDir.exists()) {
     uploadDir.mkdirs();
   }
   return dateStr;
 }
}
