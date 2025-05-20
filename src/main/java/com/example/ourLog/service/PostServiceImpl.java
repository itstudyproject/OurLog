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

    Page<Object[]> result = postRepository.searchPage(
        boardNo,
        pageRequestDTO.getKeyword(),
        pageable
    );

    Map<Long, List<Object[]>> groupedResult = result.getContent().stream()
        .collect(Collectors.groupingBy(arr -> ((Post) arr[0]).getPostId()));

    // 그룹화된 결과를 바탕으로 PostDTO 리스트 생성
    List<PostDTO> postDTOList = groupedResult.entrySet().stream()
        .map(entry -> {
          List<Object[]> postRows = entry.getValue();
          Post post = (Post) postRows.get(0)[0]; // 해당 Post의 첫 번째 행에서 Post 엔티티 가져옴
          User user = (User) postRows.get(0)[2]; // Post 엔티티에서 User 정보 가져옴

          // 해당 Post에 속한 모든 Picture 엔티티들을 수집
          List<Picture> pictures = postRows.stream()
              .map(arr -> (Picture) arr[1]) // Object[] 배열의 두 번째 요소가 Picture라고 가정
              .filter(Objects::nonNull) // null인 Picture는 제외 (사진이 없는 게시글)
              .collect(Collectors.toList());

          Trade trade = (Trade) postRows.get(0)[3];
          return entityToDTO(
              post,
              pictures,
              user,
              trade
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
