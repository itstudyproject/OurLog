package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PictureService {
  List<PictureDTO> uploadFiles(List<MultipartFile> files);

  PictureDTO getPictureById(Long picId);

  void assignPicturesToPost(List<String> uuids, Long postId);

  default Picture dtoToEntity(PictureDTO pictureDTO) {
    Picture picture = Picture.builder()
        .uuid(pictureDTO.getUuid())
        .post(Post.builder().build())
        .path(pictureDTO.getPath())
        .user(User.builder().build())
        .thumbnailImagePath(pictureDTO.getThumbnailImagePath())
        .downloads(pictureDTO.getDownloads())
        .originImagePath(pictureDTO.getOriginImagePath())
        .picId(pictureDTO.getPicId())
        .picName(pictureDTO.getPicName())
        .resizedImagePath(pictureDTO.getResizedImagePath())
        .build();
    return picture;
  }

  default PictureDTO entityToDTO(Picture picture) {
    PictureDTO pictureDTO = PictureDTO.builder()
        .postDTO(PostDTO.builder().build())
        .userDTO(UserDTO.builder().build())
        .picId(picture.getPicId())
        .uuid(picture.getUuid())
        .downloads(picture.getDownloads())
        .originImagePath(picture.getOriginImagePath())
        .path(picture.getPath())
        .resizedImagePath(picture.getResizedImagePath())
        .picName(picture.getPicName())
        .thumbnailImagePath(picture.getThumbnailImagePath())
        .build();
    return pictureDTO;
  }
}
