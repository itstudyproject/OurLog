package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Trade;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PictureRepository pictureRepository;
  private final ReplyRepository replyRepository;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  // 🔍 일반 게시글 목록 조회
  @Override
  public PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO, Long boardNo) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("postId").descending());

    // ✅ Repository에서 Post와 User만 페이징하여 가져옴
    Page<Object[]> result = postRepository.searchPage(
        boardNo,
        pageRequestDTO.getKeyword(),
        pageable
    );

    // ✅ 가져온 Object[]에서 Post 엔티티 목록만 추출
    List<Post> postList = result.getContent().stream()
        .map(arr -> (Post) arr[0]) // Object[]의 첫 번째 요소가 Post라고 가정
        .collect(Collectors.toList());

    // ✅ 추출된 Post들의 ID 목록 생성
    List<Long> postIds = postList.stream()
        .map(Post::getPostId)
        .collect(Collectors.toList());

    // ✅ Post ID 목록으로 Picture 목록 일괄 로딩
    List<Picture> pictures = postIds.isEmpty() ? Collections.emptyList() : postRepository.findPicturesByPostIds(postIds);

    // ✅ Picture 목록을 postId 기준으로 그룹화 (나중에 PostDTO에 매핑하기 위해)
    Map<Long, List<Picture>> picturesByPostId = pictures.stream()
        .collect(Collectors.groupingBy(picture -> picture.getPost().getPostId()));

    // ✅ Post ID 목록으로 Trade 목록 일괄 로딩
    List<Trade> trades = postIds.isEmpty() ? Collections.emptyList() : postRepository.findTradesByPostIds(postIds);

    // ✅ Trade 목록을 postId 기준으로 맵으로 변환 (PostDTO에 매핑하기 위해)
    Map<Long, Trade> tradesByPostId = trades.stream()
        .collect(Collectors.toMap(trade -> trade.getPost().getPostId(), trade -> trade));


    // ✅ Post 목록을 순회하며 PostDTO 생성 (가져온 Picture 및 Trade 매핑)
    List<PostDTO> postDTOList = postList.stream()
        .map(post -> {
          // 해당 Post ID에 맞는 Picture 리스트와 Trade 객체를 가져옴
          List<Picture> postPictures = picturesByPostId.getOrDefault(post.getPostId(), Collections.emptyList());
          Trade postTrade = tradesByPostId.get(post.getPostId()); // Trade는 1:1 관계이므로 get으로 가져옴

          // User 정보는 searchPage 쿼리에서 이미 가져온 Object[]에 포함되어 있을 수 있지만,
          // Repository 쿼리를 수정했으므로 Post 엔티티에서 직접 User를 가져옴
          User user = post.getUser(); // Post 엔티티에 User가 로딩되어 있다고 가정 (JPA 기본 동작 또는 EntityGraph 필요)

          // entityToDTO 메서드는 Post, List<Picture>, User, Trade를 파라미터로 받음
          // Reply Count는 현재 페이징 쿼리에서 가져오지 않으므로 0 또는 다른 기본값 설정 필요
          // (Reply Count가 목록에서 꼭 필요하다면, Repository 쿼리를 다시 고려하거나 별도 로딩 로직 추가 필요)
          Long replyCnt = 0L; // 현재 쿼리에서 가져오지 않음. 필요시 수정.


          return entityToDTO(
              post,
              postPictures,
              user,
              postTrade
          );
        })
        .collect(Collectors.toList());

    // PageResultDTO 생성 시, 원래 Page 객체의 정보(totalPage, totalCount 등)와
    // 새로 만든 postDTOList를 사용합니다.
    return new PageResultDTO<>(result, postDTOList);
  }

  // 📝 게시글 등록
  @Transactional
  @Override
  public Long register(PostDTO postDTO) {
    Map<String, Object> entityMap = dtoToEntity(postDTO);
    Post post = (Post) entityMap.get("post");

    User user = post.getUser();

    postRepository.save(post);

    List<PictureDTO> pictureDTOList = postDTO.getPictureDTOList();
    if (pictureDTOList != null && !pictureDTOList.isEmpty()) {
      for (PictureDTO pictureDTO : pictureDTOList) {
        Picture picture = pictureRepository.findByUuid(pictureDTO.getUuid());
        if (picture != null && picture.getPost() == null) {
          picture.setPost(post);
          pictureRepository.save(picture);
        }
      }
    }

    return post.getPostId();
  }

  // ✏️ 게시글 수정
  @Transactional
  @Override
  public void modify(PostDTO postDTO) {
    Optional<Post> result = postRepository.findById(postDTO.getPostId());
    if (result.isPresent()) {
      Post post = result.get();

      post.changeTitle(postDTO.getTitle());
      post.changeContent(postDTO.getContent());
      postRepository.save(post);

      List<Picture> oldPictures = pictureRepository.findByPostId(post.getPostId());
      List<String> newUUIDList = postDTO.getPictureDTOList()
              .stream()
              .map(PictureDTO::getUuid)
              .toList();

      for (Picture oldPicture : oldPictures) {
        if (!newUUIDList.contains(oldPicture.getUuid())) {
          pictureRepository.deleteByUuid(oldPicture.getUuid());
        }
      }

      for (String uuid : newUUIDList) {
        Picture picture = pictureRepository.findByUuid(uuid);
        if (picture != null && (picture.getPost() == null || !picture.getPost().equals(post))) {
          picture.setPost(post);
          pictureRepository.save(picture);
        }
      }
    }
  }

  // ❌ 게시글 삭제 (그림 + 댓글 + 파일 삭제 포함)
  @Transactional
  @Override
  public List<String> removeWithReplyAndPicture(Long postId) {
    List<Picture> pictureList = pictureRepository.findByPostId(postId);
    List<String> removedFileNames = new ArrayList<>();

    for (Picture picture : pictureList) {
      removedFileNames.add(picture.getPath() + File.separator + picture.getUuid() + "_" + picture.getPicName());
      pictureRepository.deleteByUuid(picture.getUuid());
    }

    replyRepository.deleteByPostId(postId);
    postRepository.deleteById(postId);

    return removedFileNames;
  }

  // 🧹 단일 그림 삭제
  @Override
  public void removePictureByUUID(String uuid) {
    pictureRepository.deleteByUuid(uuid);
  }

  @Override
  public List<PostDTO> getPostByUserId(Long userId) {
    List<Post> postList = postRepository.findByUser_UserId(userId);

    return postList.stream()
        .map(postDTOList -> PostDTO.builder()
            .postId(postDTOList.getPostId())
            .title(postDTOList.getTitle())
            .views(postDTOList.getViews())
            .build())
        .collect(Collectors.toList());
  }

  // 📖 게시글 상세 조회 (+ 조회수 증가)
  @Transactional
  @Override
  public PostDTO get(Long postId) {
    List<Object[]> result = postRepository.getPostWithAll(postId);
    if (result == null || result.isEmpty()) return null;

    Post post = (Post) result.get(0)[0];

    // ✅ 조회수 증가 처리
    post.increaseViews();
    postRepository.save(post);

    List<Picture> pictureList = new ArrayList<>();
    for (Object[] arr : result) {
      Picture picture = (Picture) arr[1];
      if (picture != null) {
        pictureList.add(picture);
      }
    }

    User user = (User) result.get(0)[2];
    Long replyCnt = (Long) result.get(0)[3];
    Trade trade = (Trade) result.get(0)[4];

    return entityToDTO(post, pictureList, user, trade);
  }

  @Override
  public List<PostDTO> getAllPosts() {
    // 전체 게시글 목록을 가져오기
    List<Post> posts = postRepository.findAll();

    // 각 게시글에 대해 DTO로 변환하여 반환
    List<PostDTO> postDTOs = posts.stream()
            .map(post -> {
              List<Picture> pictureList = pictureRepository.findByPostId(post.getPostId());
              User user = post.getUser();
              return entityToDTO(post, pictureList, user, null);
            })
            .collect(Collectors.toList());

    return postDTOs;
  }

  // dtoToEntity와 entityToDTO 메서드...
}
