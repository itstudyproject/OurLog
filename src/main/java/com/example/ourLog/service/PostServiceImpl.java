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
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PictureRepository pictureRepository;
  private final ReplyRepository replyRepository;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @Override
  public void increaseViews(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

    // 🔐 연관 유저 정보가 없으면 save하지 않고 그냥 return
    if (post.getUser() == null || post.getUserProfile() == null) {
      log.warn("❌ writer or profile is null. postId = {}", postId);
      return;
    }

    post.setViews(Optional.ofNullable(post.getViews()).orElse(0L) + 1);
    postRepository.save(post);
  }

  @Override
  public PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO, Long boardNo) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("postId").descending());
    Page<Object[]> result;

    // ✅ 1. 통합 검색일 경우: 제목 + 내용 + 태그 + 작성자
    if ("all".equalsIgnoreCase(pageRequestDTO.getType())) {
      result = postRepository.searchAllFields(boardNo, pageRequestDTO.getKeyword(), pageable);

      // ✅ 2. 기본: 제목만 검색
    } else {
      result = postRepository.searchPage(boardNo, pageRequestDTO.getKeyword(), pageable);
    }

    List<Post> postList = result.getContent().stream()
        .map(arr -> (Post) arr[0])
        .collect(Collectors.toList());

    List<Long> postIds = postList.stream().map(Post::getPostId).collect(Collectors.toList());

    List<Picture> pictures = postIds.isEmpty() ? Collections.emptyList() : postRepository.findPicturesByPostIds(postIds);
    Map<Long, List<Picture>> picturesByPostId = pictures.stream()
        .collect(Collectors.groupingBy(picture -> picture.getPost().getPostId()));

    List<Trade> trades = postIds.isEmpty() ? Collections.emptyList() : postRepository.findTradesByPostIds(postIds);
    Map<Long, Trade> tradesByPostId = trades.stream()
        .collect(Collectors.toMap(trade -> trade.getPost().getPostId(), trade -> trade));

    List<PostDTO> postDTOList = postList.stream()
        .map(post -> {
          List<Picture> postPictures = picturesByPostId.getOrDefault(post.getPostId(), Collections.emptyList());
          Trade postTrade = tradesByPostId.get(post.getPostId());
          User user = post.getUser();
          return entityToDTO(post, postPictures, user, postTrade);
        })
        .collect(Collectors.toList());

    return new PageResultDTO<>(result, postDTOList);
  }

  @Transactional
  @Override
  public Long register(PostDTO postDTO) {
    Map<String, Object> entityMap = dtoToEntity(postDTO);
    Post post = (Post) entityMap.get("post");
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

  @Transactional
  @Override
  public void modify(PostDTO postDTO) {
    Optional<Post> result = postRepository.findById(postDTO.getPostId());
    if (result.isPresent()) {
      Post post = result.get();
      post.changeTitle(postDTO.getTitle());
      post.changeContent(postDTO.getContent());
      post.setFileName(postDTO.getFileName());
      post.setTag(postDTO.getTag());
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

  @Override
  public void removePictureByUUID(String uuid) {
    pictureRepository.deleteByUuid(uuid);
  }

  @Override
  public List<PostDTO> getPostByUserId(Long userId) {
    List<Post> postList = postRepository.findByUser_UserId(userId);
    return postList.stream()
        .map(post -> PostDTO.builder()
            .postId(post.getPostId())
            .title(post.getTitle())
            .views(post.getViews())
            .build())
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public PostDTO get(Long postId) {
    List<Object[]> result = postRepository.getPostWithAll(postId);
    if (result == null || result.isEmpty()) return null;

    Post post = (Post) result.get(0)[0];


    List<Picture> pictureList = new ArrayList<>();
    for (Object[] arr : result) {
      Picture picture = (Picture) arr[1];
      if (picture != null) pictureList.add(picture);
    }

    User user = (User) result.get(0)[2];
    Long replyCnt = (Long) result.get(0)[3];
    Trade trade = (Trade) result.get(0)[4];

    return entityToDTO(post, pictureList, user, trade);
  }

  @Override
  public List<PostDTO> getAllPosts() {
    List<Post> posts = postRepository.findAll();
    return posts.stream()
        .map(post -> {
          List<Picture> pictureList = pictureRepository.findByPostId(post.getPostId());
          User user = post.getUser();
          return entityToDTO(post, pictureList, user, null);
        })
        .collect(Collectors.toList());
  }

  // ✅ 접근제한자 반드시 public
  @Override
  public PostDTO entityToDTO(Post post, List<Picture> pictureList, User user, Trade trade) {
    PostDTO dto = PostDTO.builder()
        .postId(post.getPostId())
        .boardNo(post.getBoardNo())
        .title(post.getTitle())
        .content(post.getContent())
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .views(post.getViews())
        .tag(post.getTag())
        .userId(user.getUserId())
        .userDTO(UserDTO.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .build())
        .build();

    if (pictureList != null && !pictureList.isEmpty()) {
      // post.getFileName()에 해당하는 Picture를 찾아서 썸네일로 설정
      Picture thumbnail = pictureList.stream()
          .filter(pic -> pic.getPicName().equals(post.getFileName()))
          .findFirst()
          .orElse(pictureList.get(0)); // 못 찾으면 첫 번째 걸로

      dto.setUuid(thumbnail.getUuid());
      dto.setPath(thumbnail.getPath());
      dto.setFileName(thumbnail.getPicName());
    }

    dto.setPictureDTOList(
        pictureList.stream()
            .map(pic -> PictureDTO.builder()
                .uuid(pic.getUuid())
                .path(pic.getPath())
                .picName(pic.getPicName())
                .build())
            .collect(Collectors.toList())
    );

    return dto;
  }
}
