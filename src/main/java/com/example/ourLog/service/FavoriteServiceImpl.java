package com.example.ourLog.service;

import com.example.ourLog.dto.FavoriteDTO;
import java.util.List;
import java.util.stream.Collectors;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.FavoriteRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  @Override
  @Transactional
  public FavoriteDTO toggleFavorite(Long userId, Long postId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    Optional<Favorite> favoriteOpt = favoriteRepository.findByUserAndPost(user, post);

    FavoriteDTO resultDto;

    if (favoriteOpt.isPresent()) {
      favoriteRepository.deleteByUserAndPost(user, post);

      // ✅ Post 엔티티의 좋아요 수 감소 및 저장
      post.decreaseFavoriteCnt();
      postRepository.save(post); // Post 엔티티 저장

      resultDto = FavoriteDTO.builder()
          .userId(userId)
          .postId(postId)
          .favorited(false) // 좋아요 취소됨
          .build();
    } else {
      Favorite favorite = Favorite.builder()
          .user(user)
          .post(post)
          .favorited(true)
          .build();

      Favorite saved = favoriteRepository.save(favorite); // Favorite 레코드 저장

      // ✅ Post 엔티티의 좋아요 수 증가 및 저장
      post.increaseFavoriteCnt();
      postRepository.save(post); // Post 엔티티 저장

      resultDto = FavoriteDTO.builder()
          .favoriteId(saved.getFavoriteId()) // 새로 생성된 ID
          .userId(userId)
          .postId(postId)
          .favorited(true) // 좋아요 추가됨
          .regDate(saved.getRegDate()) // BaseEntity 상속 시
          .modDate(saved.getModDate()) // BaseEntity 상속 시
          .build();
    }
    return resultDto;
  }


  @Override
  public boolean isFavorited(Long userId, Long postId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return favoriteRepository.existsByUserAndPost(user, post);
  }

  @Override
  public Long getFavoriteCount(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    return post.getFavoriteCnt() != null ? post.getFavoriteCnt() : 0L;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PostDTO> getFavoritesByUser(Long userId) {
    User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<Favorite> favoriteList = favoriteRepository.findByUser(user);

    return favoriteList.stream()
        .map(favorite -> {
          Post post = favorite.getPost();
          if (post != null) {
            // ✅ Post 엔티티를 PostDTO로 변환하는 로직 호출
            //    이 메소드는 Post 엔티티와 그에 연결된 Picture 정보를 바탕으로 PostDTO를 생성해야 합니다.
            //    PostDTO에는 title 및 image 정보(pictureDTOList 또는 imageUrl)가 포함되어야 합니다.
            return convertPostToDTO(post); // <-- Post 엔티티를 PostDTO로 변환하는 메소드 호출
          }
          return null; // Post가 null인 경우 (예외 처리 또는 필터링)
        })
        .filter(postDto -> postDto != null)
        .collect(Collectors.toList());
  }

  private PostDTO convertPostToDTO(Post post) {
    List<Picture> pictures = post.getPictureList(); // Post 엔티티에 getPictureList() 메소드가 있다고 가정

    // ✅ Picture 엔티티 목록을 PictureDTO 목록으로 변환하는 로직 (새로운 메소드 또는 서비스 필요)
    List<PictureDTO> pictureDTOList = pictures.stream()
        .map(this::convertPictureToDTO) // Picture 엔티티를 PictureDTO로 변환하는 메소드 호출
        .collect(Collectors.toList());

    // ✅ PictureDTO 목록에서 프론트엔드에서 사용할 이미지 URL 생성 (MyPage.tsx의 로직 참고)
    String imageUrl = pictureDTOList != null && !pictureDTOList.isEmpty()
        ? getImageUrlFromPictureDTO(pictureDTOList.get(0)) // 첫 번째 PictureDTO로 이미지 URL 생성
        : "/default-image.png"; // 이미지가 없을 경우 기본 이미지

    return PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .pictureDTOList(pictureDTOList) // ✅ PictureDTO 목록 설정
        .favoriteCnt(post.getFavoriteCnt()) // Post 엔티티에 favoriteCnt가 있다면 설정
        // ... 필요한 다른 Post 필드 추가 ...
        .build();
  }

  // ✅ Picture 엔티티를 PictureDTO로 변환하는 메소드 (예시)
  private PictureDTO convertPictureToDTO(Picture picture) {
    return PictureDTO.builder()
        .picId(picture.getPicId())
        .originImagePath(picture.getOriginImagePath())
        .resizedImagePath(picture.getResizedImagePath())
        .thumbnailImagePath(picture.getThumbnailImagePath())
        .build();
  }

  private String getImageUrlFromPictureDTO(PictureDTO pictureDTO) {
    String imagePath = pictureDTO.getResizedImagePath() != null
        ? pictureDTO.getResizedImagePath()
        : (pictureDTO.getThumbnailImagePath() != null
        ? pictureDTO.getThumbnailImagePath()
        : pictureDTO.getOriginImagePath());
    return "http://localhost:8080/ourlog/picture/display/" + imagePath; // imageBaseUrl과 동일한 경로 사용
  }

}