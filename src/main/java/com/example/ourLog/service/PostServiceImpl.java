package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
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
  public PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("postId").descending());

    Page<Object[]> result = postRepository.searchPage(
        pageRequestDTO.getType(),
        pageRequestDTO.getKeyword(),
        pageable
    );

    Function<Object[], PostDTO> fn = (arr -> entityToDTO(
        (Post) arr[0],
        List.of((Picture) arr[1]),
        (User) arr[2]
    ));

    return new PageResultDTO<>(result, fn);
  }

//  // 🔥 인기순 게시글 목록 조회 (조회수 기준)
//  @Override
//  public PageResultDTO<PostDTO, Object[]> getPopularList(PageRequestDTO pageRequestDTO) {
//    Pageable pageable = pageRequestDTO.getPageable(Sort.by("views").descending());
//
//    Page<Object[]> result = postRepository.getPopularPosts(pageable);
//
//    Function<Object[], PostDTO> fn = (arr -> entityToDTO(
//        (Post) arr[0],
//        List.of((Picture) arr[1]),
//        (User) arr[2],
//        (Long) arr[3]
//    ));
//
//    return new PageResultDTO<>(result, fn);
//  }

  // 📝 게시글 등록
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
      pictureList.add((Picture) arr[1]);
    }

    User user = (User) result.get(0)[2];
    Long replyCnt = (Long) result.get(0)[3];

    return entityToDTO(post, pictureList, user);
  }
}
