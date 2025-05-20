package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PostService {

  // PostService.java
  PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO, Long boardNo);

  // 🔥 인기순 게시글 목록 조회 (조회수 기준)
//  PageResultDTO<PostDTO, Object[]> getPopularList(PageRequestDTO pageRequestDTO);


  Long register(PostDTO postDTO);

  PostDTO get(Long postId);

  void modify(PostDTO postDTO);

  List<String> removeWithReplyAndPicture(Long postId);

  void removePictureByUUID(String uuid);

  // ✅ 전체 게시글 가져오기 (페이징 없이)
  List<PostDTO> getAllPosts();

  List<PostDTO> getPostByUserId(Long userId);

  default Map<String, Object> dtoToEntity(PostDTO postDTO) {
    Map<String, Object> entityMap = new HashMap<>();


    Post post = Post.builder()
        .postId(postDTO.getPostId())
        .title(postDTO.getTitle())
        .content(postDTO.getContent())
        .tag(postDTO.getTag())
        .fileName(postDTO.getFileName())
        .boardNo(postDTO.getBoardNo())
        .user(User.builder()
            .userId(postDTO.getUserId())
            .nickname(postDTO.getNickname())
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
  default PostDTO entityToDTO(Post post, List<Picture> pictureList, User user) {
    // 유저 DTO 생성
    UserDTO userDTO = UserDTO.builder()
        .userId(user.getUserId())
        .nickname(user.getNickname())
        .build();

    // 🔥 유저 프로필 DTO 생성
    UserProfileDTO userProfileDTO = null;
    if (post.getUserProfile() != null) {
      User profileUser = post.getUserProfile().getUser();
      userProfileDTO = UserProfileDTO.builder()
          .profileId(post.getUserProfile().getProfileId())
              .userId(profileUser.getUserId()) // ✅ 수정된 부분
          .introduction(post.getUserProfile().getIntroduction())
          .originImagePath(post.getUserProfile().getOriginImagePath())
          .thumbnailImagePath(post.getUserProfile().getThumbnailImagePath())
          .build();
    }

    // PostDTO 생성
    PostDTO postDTO = PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .tag(post.getTag())
        .fileName(post.getFileName())
        .boardNo(post.getBoardNo())
        .replyCnt(post.getReplyCnt())
        .views(post.getViews())
        .followers(post.getFollowers())
        .downloads(post.getDownloads())
        .userId(post.getUser().getUserId())
        .nickname(post.getUser().getNickname())
        .favoriteCnt(Favorite.builder()
            .build().getFavoriteCnt())
        .profileImage(UserProfile.builder()
            .build().getThumbnailImagePath())
        .thumbnailImagePath(Picture.builder()
            .build().getThumbnailImagePath())
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .build();

    if (post.getUserProfile() != null) {
      System.out.println("== userProfile is not null ==");
      System.out.println("== nickname: " + post.getUserProfile().getUser().getNickname());
    }

    if (pictureList != null && !pictureList.isEmpty()) {
      List<PictureDTO> pictureDTOList = pictureList.stream()
          .filter(p -> p != null)
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
