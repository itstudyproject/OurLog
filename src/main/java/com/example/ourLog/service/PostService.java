package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.Picture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PostService {
  PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO);

  Long register(PostDTO postDTO);

  PostDTO get(Long postId);

  void modify(PostDTO postDTO);

  List<String> removeWithReplyAndPicture(Long postId);

  void removePicturebyUUID(String uuid);

  default Map<String, Object> dtoToEntity(PostDTO postDTO) {
    System.out.println(">>>"+postDTO);
    Map<String, Object> entityMap = new HashMap<>();

    Post post = Post.builder()
        .postId(postDTO.getPostId())
        .title(postDTO.getTitle())
        .content(postDTO.getContent())
        .userId(User.builder().userId(postDTO.getUserDTO().getUserId()).build())
        .build();
    System.out.println(">>>"+post);
    entityMap.put("post", post);

    List<PictureDTO> pictureDTOList = postDTO.getPictureDTOList();
    if (pictureDTOList != null && pictureDTOList.size() > 0) {
      List<Picture> pictureList = pictureDTOList.stream().map(pictureDTO -> {
        Picture picture = Picture.builder()
            .path(pictureDTO.getPath())
            .picName(pictureDTO.getPicName())
            .uuid(pictureDTO.getUuid())
            .postId(post)
            .build();
        return picture;
      }).collect(Collectors.toList());
      entityMap.put("pictureList", pictureList);
    }
    return entityMap;
  }

  default PostDTO entityToDTO(Post post, List<Picture> pictureList,
                                 User user,Long likes, Long replyCnt) {

    UserDTO userDTO = UserDTO.builder()
        .userId(user.getUserId())
        .name(user.getName())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .mobile(user.getMobile())
        .build();
    PostDTO postDTO = PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .userDTO(userDTO)
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .build();
    List<PictureDTO> pictureDTOList = new ArrayList<>();
    if (pictureList.size() > 0 && pictureList.get(0) != null) {
      pictureDTOList = pictureList.stream().map(picture -> {
        PictureDTO pictureDTO = PictureDTO.builder()
            .picName(picture.getPicName())
            .path(picture.getPath())
            .uuid(picture.getUuid())
            .build();
        return pictureDTO;
      }).collect(Collectors.toList());
    }
    postDTO.setPictureDTOList(pictureDTOList);
    postDTO.setLikes(likes);
    postDTO.setReplyCnt(replyCnt);
    return postDTO;
  }

}