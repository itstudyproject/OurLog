package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PostService {

  PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO, Long boardNo);

  // 🔥 인기순 게시글 목록 조회 (조회수 기준)
//  PageResultDTO<PostDTO, Object[]> getPopularList(PageRequestDTO pageRequestDTO);

  Long register(PostDTO postDTO);

  PostDTO get(Long postId);

  void modify(PostDTO postDTO);

  List<String> removeWithReplyAndPicture(Long postId);

  void removePictureByUUID(String uuid);

  default Map<String, Object> dtoToEntity(PostDTO postDTO) {
    Map<String, Object> entityMap = new HashMap<>();

    // tags(List<String>) → tag(String) 변환
    String tagString = null;
    if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
      tagString = String.join(",", postDTO.getTags());
    } else {
      tagString = postDTO.getTag(); // 혹시 단일 태그로 오는 경우
    }

    Post post = Post.builder()
        .postId(postDTO.getPostId())
        .title(postDTO.getTitle())
        .content(postDTO.getContent())
        .tag(tagString)
        .fileName(postDTO.getFileName())
        .boardNo(postDTO.getBoardNo())
        .user(User.builder()
            .userId(postDTO.getUserDTO().getUserId())
            .nickname(postDTO.getUserDTO().getNickname())
            .build())
        .build();

    entityMap.put("post", post);

    List<PictureDTO> pictureDTOList = postDTO.getPictureDTOList();
    if (pictureDTOList != null && !pictureDTOList.isEmpty()) {
      List<Picture> pictureList = pictureDTOList.stream()
          .map(dto -> Picture.builder()
              .uuid(dto.getUuid())
              .picName(dto.getPicName())
              .path(dto.getPath())
              .post(null)
              .build())
          .collect(Collectors.toList());
      entityMap.put("pictureList", pictureList);
    }

    return entityMap;
  }

  // ✨ Entity → DTO 변환
  default PostDTO entityToDTO(Post post, List<Picture> pictureList, User user, Long replyCount) {
    PostDTO postDTO = PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .boardNo(post.getBoardNo())
        .views(post.getViews())
        .tag(post.getTag())
        .fileName(post.getFileName())
        .replyCnt(replyCount)
        .replyCnt(replyCount != null ? replyCount : 0L)
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .build();

    if (user != null) {
      UserDTO userDTO = UserDTO.builder()
          .userId(user.getUserId())
          .email(user.getEmail())
          .nickname(user.getNickname())
          .build();
      postDTO.setUserDTO(userDTO);
    }

    if (pictureList != null && !pictureList.isEmpty()) {
      List<PictureDTO> pictureDTOList = pictureList.stream()
          .map(p -> PictureDTO.builder()
              .uuid(p.getUuid())
              .picName(p.getPicName())
              .path(p.getPath())
              .build())
          .collect(Collectors.toList());

      postDTO.setPictureDTOList(pictureDTOList);
    }

    return postDTO;
  }
}
