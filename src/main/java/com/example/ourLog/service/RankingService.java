package com.example.ourLog.service;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final PostRepository postRepository;
  private final PictureRepository pictureRepository;

  public List<PostDTO> getRankingBy(String type) {
    try {
      List<Post> posts;

      switch (type.toLowerCase()) {
        case "followers":
        case "follow":
          posts = postRepository.findAllByBoardNoOrderByFollowersDesc(5);
          break;
        case "downloads":
        case "download":
          posts = postRepository.findAllByBoardNoOrderByDownloadsDesc(5);
          break;
        case "views":
        case "view":
        default:
          posts = postRepository.findAllByBoardNoOrderByViewsDesc(5);
          break;
      }

      return posts.stream()
          .map(this::postToDTO)
          .collect(Collectors.toList());

    } catch (Exception e) {
      System.out.println("🔥 예외 발생: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }



  private PostDTO postToDTO(Post post) {
    User user = post.getUser();
    UserProfile userProfile = user.getUserProfile();

    Picture mainPicture = null;
    
    if (post.getFileName() != null && !post.getFileName().isEmpty()) {
      mainPicture = pictureRepository.findByUuid(post.getFileName()); // pictureRepository.findByUuid 메소드 필요
    }

    String thumbnailPath = null;

    if (mainPicture != null) {
      thumbnailPath = mainPicture.getThumbnailImagePath();
    }
    return PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .views(post.getViews())
        .downloads(post.getDownloads())
        .followers(post.getFollowers())
        .tag(post.getTag())
        .fileName(post.getFileName())
        .boardNo(post.getBoardNo())
        .replyCnt(post.getReplyCnt())
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .nickname(user.getNickname())
        .thumbnailImagePath(thumbnailPath)
        .build();
  }

}
