package com.example.ourLog.service;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserProfile;
import com.example.ourLog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final PostRepository postRepository;

  public List<PostDTO> getRankingBy(String type) {
    try {
      List<Post> posts;

      switch (type.toLowerCase()) {
        case "followers":
        case "follow":
          posts = postRepository.findAllByOrderByFollowersDesc();
          break;
        case "downloads":
        case "download":
          posts = postRepository.findAllByOrderByDownloadsDesc();
          break;
        case "views":
        case "view":
        default:
          posts = postRepository.findAllByOrderByViewsDesc();
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
        .userProfileDTO(userProfile != null ? userProfile.toDTOWithUser() : null)
        .build();
  }

}
